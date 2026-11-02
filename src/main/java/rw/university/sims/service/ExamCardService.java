package rw.university.sims.service;

import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rw.university.sims.dto.response.*;
import rw.university.sims.entity.*;
import rw.university.sims.enums.*;
import rw.university.sims.exception.ResourceConflictException;
import rw.university.sims.exception.ResourceNotFoundException;
import rw.university.sims.repository.*;
import java.util.*;

@Service
@Transactional
public class ExamCardService {
    private final ExamCardRepository cardRepository;
    private final ExamCardAttemptRepository attemptRepository;
    private final CourseRegistrationRepository registrationRepository;
    private final StudentRepository studentRepository;
    private final UserAccountRepository userRepository;
    private final FeeBalanceCalculator balanceCalculator;
    private final ExamCardAttemptRecorder attemptRecorder;
    private final ExamCardWriter cardWriter;
    private final AuditService auditService;
    public ExamCardService(ExamCardRepository cardRepository,ExamCardAttemptRepository attemptRepository,
            CourseRegistrationRepository registrationRepository,StudentRepository studentRepository,UserAccountRepository userRepository,
            FeeBalanceCalculator balanceCalculator,ExamCardAttemptRecorder attemptRecorder,ExamCardWriter cardWriter,AuditService auditService){
        this.cardRepository=cardRepository;this.attemptRepository=attemptRepository;this.registrationRepository=registrationRepository;
        this.studentRepository=studentRepository;this.userRepository=userRepository;this.balanceCalculator=balanceCalculator;
        this.attemptRecorder=attemptRecorder;this.cardWriter=cardWriter;this.auditService=auditService;
    }
    public ExamCardGenerationOutcome generate(Long registrationId,Long actorId,String ip){
        UserAccount actor=requireUser(actorId); Student student=studentForActor(actor);
        CourseRegistration registration=registrationRepository.findDetailedById(registrationId).orElse(null);
        if(registration==null){attemptRecorder.record(student.getId(),null,null,ExamCardAttemptOutcome.REFUSED,"REGISTRATION_INVALID",actor.getId(),ip); throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"REGISTRATION_INVALID");}
        if(!Objects.equals(registration.getStudent().getId(),student.getId())){attemptRecorder.record(student.getId(),registration.getId(),null,ExamCardAttemptOutcome.REFUSED,"REGISTRATION_INVALID",actor.getId(),ip);throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You may only generate a card for your own registration.");}
        if(student.getStatus()!=StudentStatus.ACTIVE){attemptRecorder.record(student.getId(),registration.getId(),null,ExamCardAttemptOutcome.REFUSED,"STUDENT_NOT_ACTIVE",actor.getId(),ip);throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"STUDENT_NOT_ACTIVE");}
        if(registration.getStatus()!=RegistrationStatus.REGISTERED){attemptRecorder.record(student.getId(),registration.getId(),null,ExamCardAttemptOutcome.REFUSED,"REGISTRATION_INVALID",actor.getId(),ip);throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"REGISTRATION_INVALID");}
        Optional<ExamCard> existing=cardRepository.findByCourseRegistration_Id(registrationId);
        if(existing.isPresent()){
            attemptRecorder.record(student.getId(),registration.getId(),null,ExamCardAttemptOutcome.REFUSED,"ALREADY_ISSUED",actor.getId(),ip);
            return new ExamCardGenerationOutcome(response(existing.get()),false);
        }
        Long semesterId=registration.getCourseOffering().getSemester().getId();
        if(!balanceCalculator.calculate(student.getId(),semesterId).canSitExams()){
            attemptRecorder.record(student.getId(),registration.getId(),null,ExamCardAttemptOutcome.REFUSED,"FEES_NOT_PAID",actor.getId(),ip);
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"FEES_NOT_PAID");
        }
        String cardNumber="UGL-EXAM-"+UUID.randomUUID().toString().replace("-","").substring(0,20).toUpperCase(Locale.ROOT);
        cardWriter.create(registration.getId(),student.getId(),semesterId,actor.getId(),cardNumber);
        ExamCard card=cardRepository.findByCourseRegistration_Id(registrationId)
                .orElseThrow(()->new ResourceNotFoundException("Generated exam card could not be reloaded."));
        attemptRecorder.record(student.getId(),registration.getId(),card.getId(),ExamCardAttemptOutcome.ISSUED,null,actor.getId(),ip);
        auditService.record(actor,"EXAM_CARD_ISSUED","EXAM_CARD",card.getId(),"SUCCESS",ip,null);
        return new ExamCardGenerationOutcome(response(card),true);
    }
    @Transactional(readOnly = true)
    public PageResponse<ExamCardResponse> mine(int page,int size,String direction,Long actorId){
        UserAccount actor=requireUser(actorId);Student student=studentForActor(actor);
        Pageable pageable=pageable(page,size,direction);
        Page<ExamCardResponse> result=cardRepository.findByStudent_Id(student.getId(),pageable).map(this::response);
        return new PageResponse<>(result.getContent(),result.getNumber(),result.getSize(),result.getTotalElements(),result.getTotalPages());
    }
    @Transactional(readOnly = true)
    public ExamCardVerificationResponse verify(String cardNumber,Long actorId){
        UserAccount actor=requireUser(actorId);
        ExamCard card=cardRepository.findDetailedByCardNumber(cardNumber).orElseThrow(()->new ResourceNotFoundException("Exam card not found."));
        enforceVerificationScope(actor,card);
        CourseOffering offering=card.getCourseRegistration().getCourseOffering();
        boolean valid=card.getStatus()==ExamCardStatus.ISSUED;
        return new ExamCardVerificationResponse(card.getCardNumber(),valid,card.getStatus(),card.getStudent().getRegistrationNumber(),
            card.getStudent().getFirstName()+" "+card.getStudent().getLastName(),offering.getCourse().getCourseCode(),offering.getCourse().getTitle(),
            offering.getSection(),card.getSemester().getName(),card.getIssuedAt());
    }
    @Transactional(readOnly = true)
    public PageResponse<ExamCardAttemptResponse> attempts(Long studentId,int page,int size,String direction,Long actorId){
        UserAccount actor=requireUser(actorId);Student student=studentRepository.findDetailedById(studentId).orElseThrow(()->new ResourceNotFoundException("Student not found: "+studentId));
        enforceStudentDepartmentScope(actor,student);
        Page<ExamCardAttemptResponse> result=attemptRepository.findByStudent_IdOrderByRequestedAtDesc(studentId,pageable(page,size,direction)).map(this::attemptResponse);
        return new PageResponse<>(result.getContent(),result.getNumber(),result.getSize(),result.getTotalElements(),result.getTotalPages());
    }
    private void enforceVerificationScope(UserAccount actor,ExamCard card){
        Set<String> roles=roles(actor);if(roles.contains("REGISTRAR"))return;
        Department department=departmentOf(card.getStudent());
        if((roles.contains("LECTURER")||roles.contains("DEAN"))&&actor.getDepartment()!=null&&department!=null&&Objects.equals(actor.getDepartment().getId(),department.getId()))return;
        forbidden();
    }
    private void enforceStudentDepartmentScope(UserAccount actor,Student student){
        Set<String> roles=roles(actor);if(roles.contains("REGISTRAR"))return;
        Department department=departmentOf(student);
        if((roles.contains("LECTURER")||roles.contains("DEAN"))&&actor.getDepartment()!=null&&department!=null&&Objects.equals(actor.getDepartment().getId(),department.getId()))return;
        forbidden();
    }
    private Department departmentOf(Student student){return student.getProgramme()==null?null:student.getProgramme().getDepartment();}
    private Student studentForActor(UserAccount actor){if(!roles(actor).contains("STUDENT")||actor.getStudentLink()==null||actor.getStudentLink().getStudent()==null)forbidden();
        return studentRepository.findDetailedById(actor.getStudentLink().getStudent().getId()).orElseThrow(()->new ResourceNotFoundException("Student profile not found."));}
    private UserAccount requireUser(Long id){return userRepository.findWithDetailsById(id).orElseThrow(()->new ResourceNotFoundException("User not found: "+id));}
    private Set<String> roles(UserAccount actor){Set<String>s=new HashSet<>();actor.getRoles().forEach(r->s.add(r.getName()));return s;}
    private void forbidden(){throw new ResponseStatusException(HttpStatus.FORBIDDEN,"You do not have access to this exam-card record.");}
    private Pageable pageable(int page,int size,String direction){if(page<0||size<1||size>100)throw new IllegalArgumentException("Page must be >= 0 and size must be 1 to 100.");return PageRequest.of(page,size,Sort.by("asc".equalsIgnoreCase(direction)?Sort.Direction.ASC:Sort.Direction.DESC,"issuedAt"));}
    private ExamCardResponse response(ExamCard c){CourseOffering o=c.getCourseRegistration().getCourseOffering();return new ExamCardResponse(c.getId(),c.getCardNumber(),c.getStatus(),c.getCourseRegistration().getId(),o.getCourse().getCourseCode(),o.getCourse().getTitle(),o.getSection(),c.getSemester().getId(),c.getSemester().getName(),c.getStudent().getRegistrationNumber(),c.getStudent().getFirstName()+" "+c.getStudent().getLastName(),c.getIssuedAt());}
    private ExamCardAttemptResponse attemptResponse(ExamCardAttempt a){return new ExamCardAttemptResponse(a.getId(),a.getCourseRegistration()==null?null:a.getCourseRegistration().getId(),a.getExamCard()==null?null:a.getExamCard().getCardNumber(),a.getOutcome(),a.getRefusalReason(),a.getRequestedBy().getId(),a.getRequestedBy().getFullName(),a.getIpAddress(),a.getRequestedAt());}
}
