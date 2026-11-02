package rw.university.sims.service;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rw.university.sims.dto.request.FeeObligationCreateRequest;
import rw.university.sims.dto.request.PaymentCreateRequest;
import rw.university.sims.dto.response.*;
import rw.university.sims.entity.*;
import rw.university.sims.enums.*;
import rw.university.sims.exception.ResourceConflictException;
import rw.university.sims.exception.ResourceNotFoundException;
import rw.university.sims.repository.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@Transactional
public class FinanceService {
    private static final Set<String> FINANCE_ROLES=Set.of("FINANCE_OFFICER");
    private static final Set<String> GLOBAL_READ_ROLES=Set.of("FINANCE_OFFICER","REGISTRAR");
    private static final Set<String> ALLOWED_SORT=Set.of("id","createdAt","amountDue","feeType","status");
    private final FeeObligationRepository obligationRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentStatusHistoryRepository historyRepository;
    private final StudentRepository studentRepository;
    private final SemesterRepository semesterRepository;
    private final UserAccountRepository userRepository;
    private final FeeBalanceCalculator balanceCalculator;
    private final AuditService auditService;
    public FinanceService(FeeObligationRepository obligationRepository, PaymentRepository paymentRepository,
            PaymentStatusHistoryRepository historyRepository, StudentRepository studentRepository,
            SemesterRepository semesterRepository, UserAccountRepository userRepository,
            FeeBalanceCalculator balanceCalculator, AuditService auditService) {
        this.obligationRepository=obligationRepository; this.paymentRepository=paymentRepository; this.historyRepository=historyRepository;
        this.studentRepository=studentRepository; this.semesterRepository=semesterRepository; this.userRepository=userRepository;
        this.balanceCalculator=balanceCalculator; this.auditService=auditService;
    }
    public FeeObligationResponse createObligation(FeeObligationCreateRequest request,Long actorId,String ip){
        UserAccount actor=requireFinance(actorId);
        Student student=studentRepository.findDetailedById(request.studentId()).orElseThrow(()->new ResourceNotFoundException("Student not found: "+request.studentId()));
        Semester semester=semesterRepository.findById(request.semesterId()).orElseThrow(()->new ResourceNotFoundException("Semester not found: "+request.semesterId()));
        if(obligationRepository.existsByStudent_IdAndSemester_IdAndFeeType(student.getId(),semester.getId(),request.feeType())) throw new ResourceConflictException("A fee obligation of this type already exists for this student and semester.");
        String currency=normaliseCurrency(request.currency());
        FeeObligation obligation=new FeeObligation(student,semester,request.feeType(),request.amountDue(),currency,
                request.countsTowardEligibility()==null||request.countsTowardEligibility(),actor);
        obligation=obligationRepository.saveAndFlush(obligation);
        auditService.record(actor,"FEE_OBLIGATION_CREATED","FEE_OBLIGATION",obligation.getId(),"SUCCESS",ip,null);
        return obligationResponse(obligation);
    }
    @Transactional(readOnly = true)
    public PageResponse<FeeObligationResponse> listObligations(Long studentId,Long semesterId,FeeObligationStatus status,
            int page,int size,String sortBy,String direction,Long actorId){
        UserAccount actor=requireUser(actorId); Set<String> roles=roles(actor);
        if(roles.contains("STUDENT")){Long own=studentIdFrom(actor); if(studentId!=null&&!Objects.equals(own,studentId)) forbidden(); studentId=own;}
        else if(!roles.stream().anyMatch(GLOBAL_READ_ROLES::contains)) forbidden();
        Pageable pageable=pageable(page,size,sortBy,direction,Set.of("id","createdAt","amountDue","feeType","status"));
        Page<FeeObligation> result=obligationRepository.search(studentId,semesterId,status,pageable);
        return pageObligations(result.map(this::obligationResponse));
    }
    public FeeObligationResponse cancelObligation(Long id,Long actorId,String ip){
        UserAccount actor=requireFinance(actorId);
        FeeObligation item=obligationRepository.findDetailedById(id).orElseThrow(()->new ResourceNotFoundException("Fee obligation not found: "+id));
        if(item.getStatus()!=FeeObligationStatus.ACTIVE) throw new ResourceConflictException("Fee obligation is already cancelled.");
        if(paymentRepository.existsByFeeObligation_Id(id)) throw new ResourceConflictException("This fee obligation has payments allocated to it and cannot be cancelled.");
        item.setStatus(FeeObligationStatus.CANCELLED); obligationRepository.save(item);
        auditService.record(actor,"FEE_OBLIGATION_CANCELLED","FEE_OBLIGATION",id,"SUCCESS",ip,null);
        return obligationResponse(item);
    }
    public PaymentRecordOutcome recordPayment(PaymentCreateRequest request,Long actorId,String ip){
        UserAccount actor=requireFinance(actorId);
        String currency=normaliseCurrency(request.currency());
        OffsetDateTime received=request.receivedAt()==null?OffsetDateTime.now():request.receivedAt();
        String fingerprint=fingerprint(request.studentId(),request.semesterId(),request.feeObligationId(),request.amount(),currency,request.transactionReference().trim(),request.receivedAt());
        Optional<Payment> prior=paymentRepository.findByTransactionReference(request.transactionReference().trim());
        if(prior.isPresent()){
            if(!prior.get().getRequestFingerprint().equals(fingerprint)) throw new ResourceConflictException("Transaction reference has already been used with different payment details.");
            return new PaymentRecordOutcome(paymentResponse(prior.get()),false);
        }
        Student student=studentRepository.findDetailedById(request.studentId()).orElseThrow(()->new ResourceNotFoundException("Student not found: "+request.studentId()));
        Semester semester=semesterRepository.findById(request.semesterId()).orElseThrow(()->new ResourceNotFoundException("Semester not found: "+request.semesterId()));
        FeeObligation obligation=null;
        if(request.feeObligationId()!=null){
            obligation=obligationRepository.findDetailedById(request.feeObligationId()).orElseThrow(()->new ResourceNotFoundException("Fee obligation not found: "+request.feeObligationId()));
            if(!Objects.equals(obligation.getStudent().getId(),student.getId())||!Objects.equals(obligation.getSemester().getId(),semester.getId())) throw new IllegalArgumentException("The fee obligation must belong to the same student and semester as the payment.");
            if(obligation.getStatus()!=FeeObligationStatus.ACTIVE) throw new ResourceConflictException("Cannot allocate a payment to a cancelled fee obligation.");
            if(!obligation.getCurrency().equals(currency)) throw new IllegalArgumentException("Payment currency must match the allocated fee obligation.");
        }
        Payment payment=new Payment(student,semester,obligation,request.amount(),currency,request.transactionReference().trim(),fingerprint,received,actor);
        payment=paymentRepository.saveAndFlush(payment);
        auditService.record(actor,"PAYMENT_RECORDED","PAYMENT",payment.getId(),"SUCCESS",ip,null);
        return new PaymentRecordOutcome(paymentResponse(payment),true);
    }
    public PaymentResponse confirmPayment(Long id,Long actorId,String ip){return transition(id,actorId,ip,PaymentStatus.CONFIRMED);}
    public PaymentResponse failPayment(Long id,Long actorId,String ip){return transition(id,actorId,ip,PaymentStatus.FAILED);}
    public PaymentResponse reversePayment(Long id,String reason,Long actorId,String ip){
        if(reason==null||reason.isBlank()) throw new IllegalArgumentException("A reason is required to reverse a payment.");
        UserAccount actor=requireFinance(actorId); Payment p=requirePayment(id);
        if(p.getStatus()!=PaymentStatus.CONFIRMED) throw new ResourceConflictException("Only a CONFIRMED payment can be reversed.");
        p.setStatus(PaymentStatus.REVERSED); paymentRepository.save(p);
        historyRepository.save(new PaymentStatusHistory(p,PaymentStatus.CONFIRMED,PaymentStatus.REVERSED,actor,reason.trim()));
        auditService.record(actor,"PAYMENT_REVERSED","PAYMENT",id,"SUCCESS",ip,null); return paymentResponse(p);
    }
    private PaymentResponse transition(Long id,Long actorId,String ip,PaymentStatus target){
        UserAccount actor=requireFinance(actorId); Payment p=requirePayment(id);
        if(p.getStatus()!=PaymentStatus.PENDING) throw new ResourceConflictException("Only a PENDING payment can be confirmed or failed.");
        PaymentStatus from=p.getStatus(); p.setStatus(target);
        if(target==PaymentStatus.CONFIRMED){p.setConfirmedAt(OffsetDateTime.now());p.setConfirmedBy(actor);}
        paymentRepository.save(p); historyRepository.save(new PaymentStatusHistory(p,from,target,actor,null));
        auditService.record(actor,target==PaymentStatus.CONFIRMED?"PAYMENT_CONFIRMED":"PAYMENT_FAILED","PAYMENT",id,"SUCCESS",ip,null);
        return paymentResponse(p);
    }
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> listPayments(Long studentId,Long semesterId,PaymentStatus status,int page,int size,String sortBy,String direction,Long actorId){
        UserAccount actor=requireUser(actorId); if(roles(actor).stream().noneMatch(GLOBAL_READ_ROLES::contains)) forbidden();
        Pageable pageable=pageable(page,size,sortBy,direction,Set.of("id","amount","receivedAt","confirmedAt","status"));
        return pagePayments(paymentRepository.search(studentId,semesterId,status,pageable).map(this::paymentResponse));
    }
    @Transactional(readOnly = true)
    public PaymentDetailResponse getPayment(Long id,Long actorId){
        UserAccount actor=requireUser(actorId); if(roles(actor).stream().noneMatch(GLOBAL_READ_ROLES::contains)) forbidden();
        Payment p=paymentRepository.findDetailedById(id).orElseThrow(()->new ResourceNotFoundException("Payment not found: "+id));
        return new PaymentDetailResponse(paymentResponse(p),historyRepository.findByPayment_IdOrderByChangedAtDesc(id).stream()
          .map(h->new PaymentStatusHistoryResponse(h.getId(),h.getFromStatus(),h.getToStatus(),h.getChangedBy()==null?null:h.getChangedBy().getId(),h.getChangedBy()==null?null:h.getChangedBy().getFullName(),h.getReason(),h.getChangedAt())).toList());
    }
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> myPayments(int page,int size,String direction,Long actorId){
        UserAccount actor=requireUser(actorId); Long studentId=studentIdFrom(actor);
        Pageable pageable=pageable(page,size,"receivedAt",direction,Set.of("receivedAt"));
        return pagePayments(paymentRepository.findByStudent_Id(studentId,pageable).map(this::paymentResponse));
    }
    @Transactional(readOnly = true)
    public FeeBalanceResponse studentBalance(Long studentId,Long semesterId,Long actorId){
        UserAccount actor=requireUser(actorId); Set<String> roleNames=roles(actor);
        if(roleNames.contains("STUDENT")&&!Objects.equals(studentIdFrom(actor),studentId)) forbidden();
        else if(!roleNames.contains("STUDENT")&&!roleNames.stream().anyMatch(GLOBAL_READ_ROLES::contains)) forbidden();
        semesterRepository.findById(semesterId).orElseThrow(()->new ResourceNotFoundException("Semester not found: "+semesterId));
        return balanceCalculator.calculate(studentId,semesterId);
    }
    @Transactional(readOnly = true)
    public FeeBalanceResponse myBalance(Long semesterId,Long actorId){
        UserAccount actor=requireUser(actorId); Long studentId=studentIdFrom(actor);
        semesterRepository.findById(semesterId).orElseThrow(()->new ResourceNotFoundException("Semester not found: "+semesterId));
        return balanceCalculator.calculate(studentId,semesterId);
    }
    private Payment requirePayment(Long id){return paymentRepository.findDetailedById(id).orElseThrow(()->new ResourceNotFoundException("Payment not found: "+id));}
    private UserAccount requireFinance(Long id){UserAccount u=requireUser(id); if(Collections.disjoint(roles(u),FINANCE_ROLES)) forbidden(); return u;}
    private UserAccount requireUser(Long id){return userRepository.findWithDetailsById(id).orElseThrow(()->new ResourceNotFoundException("User not found: "+id));}
    private Long studentIdFrom(UserAccount u){if(!roles(u).contains("STUDENT")||u.getStudentLink()==null||u.getStudentLink().getStudent()==null) forbidden(); return u.getStudentLink().getStudent().getId();}
    private Set<String> roles(UserAccount u){Set<String> s=new HashSet<>();u.getRoles().forEach(r->s.add(r.getName()));return s;}
    private void forbidden(){throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You do not have access to this financial record.");}
    private String normaliseCurrency(String value){String currency=(value==null||value.isBlank()?"RWF":value.trim().toUpperCase(Locale.ROOT)); if(!currency.matches("[A-Z]{3}")) throw new IllegalArgumentException("Currency must be a three-letter code.");return currency;}
    private String fingerprint(Long studentId,Long semesterId,Long feeObligationId,BigDecimal amount,String currency,String reference,OffsetDateTime received){
        String canonical=studentId+"|"+semesterId+"|"+feeObligationId+"|"+amount.stripTrailingZeros().toPlainString()+"|"+currency+"|"+reference+"|"+(received==null?"AUTO":received.toInstant().toString());
        try{byte[] hash=MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8));return HexFormat.of().formatHex(hash);}catch(Exception e){throw new IllegalStateException("SHA-256 is unavailable",e);}
    }
    private Pageable pageable(int page,int size,String sortBy,String direction,Set<String> allowed){
        if(page<0||size<1||size>100) throw new IllegalArgumentException("Page must be zero or greater and size between 1 and 100.");
        if(!allowed.contains(sortBy)) throw new IllegalArgumentException("Unsupported sort field: "+sortBy);
        Sort.Direction dir="asc".equalsIgnoreCase(direction)?Sort.Direction.ASC:Sort.Direction.DESC;
        return PageRequest.of(page,size,Sort.by(dir,sortBy));
    }
    private PageResponse<FeeObligationResponse> pageObligations(Page<FeeObligationResponse> p){return new PageResponse<>(p.getContent(),p.getNumber(),p.getSize(),p.getTotalElements(),p.getTotalPages());}
    private PageResponse<PaymentResponse> pagePayments(Page<PaymentResponse> p){return new PageResponse<>(p.getContent(),p.getNumber(),p.getSize(),p.getTotalElements(),p.getTotalPages());}
    private FeeObligationResponse obligationResponse(FeeObligation f){return new FeeObligationResponse(f.getId(),f.getStudent().getId(),f.getStudent().getRegistrationNumber(),f.getStudent().getFirstName()+" "+f.getStudent().getLastName(),f.getSemester().getId(),f.getSemester().getName(),f.getFeeType(),f.getAmountDue(),f.getCurrency(),f.isCountsTowardEligibility(),f.getStatus(),f.getCreatedAt(),f.getCreatedBy().getId());}
    private PaymentResponse paymentResponse(Payment p){return new PaymentResponse(p.getId(),p.getStudent().getId(),p.getStudent().getRegistrationNumber(),p.getSemester().getId(),p.getFeeObligation()==null?null:p.getFeeObligation().getId(),p.getAmount(),p.getCurrency(),p.getTransactionReference(),p.getStatus(),p.getReceivedAt(),p.getConfirmedAt(),p.getConfirmedBy()==null?null:p.getConfirmedBy().getId(),p.getRecordedBy().getId(),p.getVersion());}
}
