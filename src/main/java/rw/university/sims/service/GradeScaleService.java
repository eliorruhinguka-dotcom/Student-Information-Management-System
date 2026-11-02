package rw.university.sims.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rw.university.sims.dto.request.GradeScaleBandRequest;
import rw.university.sims.dto.request.GradeScaleCreateRequest;
import rw.university.sims.dto.request.GradeScaleUpdateRequest;
import rw.university.sims.dto.response.GradeScaleBandResponse;
import rw.university.sims.dto.response.GradeScaleResponse;
import rw.university.sims.entity.GradeScale;
import rw.university.sims.entity.GradeScaleBand;
import rw.university.sims.entity.Role;
import rw.university.sims.entity.UserAccount;
import rw.university.sims.exception.ResourceConflictException;
import rw.university.sims.exception.ResourceNotFoundException;
import rw.university.sims.repository.AcademicResultRepository;
import rw.university.sims.repository.GradeScaleRepository;
import rw.university.sims.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional
public class GradeScaleService {
    private final GradeScaleRepository scaleRepository;
    private final AcademicResultRepository resultRepository;
    private final UserAccountRepository userRepository;
    private final AuditService auditService;
    public GradeScaleService(GradeScaleRepository scaleRepository, AcademicResultRepository resultRepository,
                             UserAccountRepository userRepository, AuditService auditService) {
        this.scaleRepository=scaleRepository; this.resultRepository=resultRepository; this.userRepository=userRepository; this.auditService=auditService;
    }
    public GradeScaleResponse create(GradeScaleCreateRequest request, Long actorId, String ip) {
        UserAccount actor=requireRegistrar(actorId);
        String name=request.name().trim();
        if(scaleRepository.existsByNameIgnoreCase(name)) throw new ResourceConflictException("A grade scale with this name already exists.");
        validateBands(request.bands());
        GradeScale scale=new GradeScale(name,request.effectiveFrom());
        request.bands().stream().sorted(Comparator.comparing(GradeScaleBandRequest::minMark))
                .map(this::toBand).forEach(scale::addBand);
        scale=scaleRepository.saveAndFlush(scale);
        auditService.record(actor,"GRADE_SCALE_CREATED","GRADE_SCALE",scale.getId(),"SUCCESS",ip,null);
        return response(scale);
    }
    @Transactional(readOnly = true)
    public List<GradeScaleResponse> list() {
        return scaleRepository.findAllByOrderByEffectiveFromDesc().stream().map(this::response).toList();
    }
    public GradeScaleResponse replace(Long id, GradeScaleUpdateRequest request, Long actorId, String ip) {
        UserAccount actor=requireRegistrar(actorId);
        GradeScale scale=scaleRepository.findDetailedById(id).orElseThrow(()->new ResourceNotFoundException("Grade scale not found: "+id));
        if(scale.isActive()) throw new ResourceConflictException("The active grade scale cannot be edited.");
        if(resultRepository.existsByGradeScale_Id(id)) throw new ResourceConflictException("This grade scale has already been used by results.");
        if(scaleRepository.existsByNameIgnoreCaseAndIdNot(request.name().trim(),id)) throw new ResourceConflictException("Another grade scale has this name.");
        validateBands(request.bands());
        scale.setName(request.name().trim()); scale.setEffectiveFrom(request.effectiveFrom());
        List<GradeScaleBand> bands=request.bands().stream().sorted(Comparator.comparing(GradeScaleBandRequest::minMark)).map(this::toBand).toList();
        scale.replaceBands(bands); scale=scaleRepository.saveAndFlush(scale);
        auditService.record(actor,"GRADE_SCALE_UPDATED","GRADE_SCALE",id,"SUCCESS",ip,null);
        return response(scale);
    }
    public GradeScaleResponse activate(Long id, Long actorId, String ip) {
        UserAccount actor=requireRegistrar(actorId);
        GradeScale target=scaleRepository.findDetailedById(id).orElseThrow(()->new ResourceNotFoundException("Grade scale not found: "+id));
        if(target.getBands().isEmpty()) throw new ResourceConflictException("A grade scale must have at least one band before activation.");
        validateExistingBands(target.getBands());
        scaleRepository.findByActiveTrue().filter(old->!Objects.equals(old.getId(),id)).ifPresent(old->{old.setActive(false); scaleRepository.saveAndFlush(old);});
        target.setActive(true); target=scaleRepository.saveAndFlush(target);
        auditService.record(actor,"GRADE_SCALE_ACTIVATED","GRADE_SCALE",id,"SUCCESS",ip,null);
        return response(target);
    }
    private void validateBands(List<GradeScaleBandRequest> bands) {
        if(bands==null||bands.isEmpty()) throw new IllegalArgumentException("At least one grade band is required.");
        List<GradeScaleBandRequest> sorted=bands.stream().sorted(Comparator.comparing(GradeScaleBandRequest::minMark)).toList();
        BigDecimal previousMax=null;
        for(GradeScaleBandRequest b:sorted){
            if(b.minMark().compareTo(b.maxMark())>0) throw new IllegalArgumentException("Each band's minimum mark must not exceed its maximum mark.");
            if(b.minMark().compareTo(BigDecimal.ZERO)<0||b.maxMark().compareTo(new BigDecimal("100"))>0) throw new IllegalArgumentException("Grade marks must be between 0 and 100.");
            if(b.gradePoints().compareTo(BigDecimal.ZERO)<0||b.gradePoints().compareTo(new BigDecimal("9.99"))>0) throw new IllegalArgumentException("Grade points must be between 0 and 9.99.");
            if(previousMax!=null&&b.minMark().compareTo(previousMax)<=0) throw new ResourceConflictException("Grade bands overlap.");
            previousMax=b.maxMark();
        }
    }
    private void validateExistingBands(List<GradeScaleBand> bands){
        List<GradeScaleBand> sorted=bands.stream().sorted(Comparator.comparing(GradeScaleBand::getMinMark)).toList();
        BigDecimal prev=null; for(GradeScaleBand b:sorted){if(b.getMinMark().compareTo(b.getMaxMark())>0||(prev!=null&&b.getMinMark().compareTo(prev)<=0)) throw new ResourceConflictException("Grade bands overlap or have an invalid range."); prev=b.getMaxMark();}
    }
    private GradeScaleBand toBand(GradeScaleBandRequest b){return new GradeScaleBand(b.minMark(),b.maxMark(),b.grade().trim(),b.gradePoints(),b.pass());}
    private UserAccount requireRegistrar(Long id){UserAccount actor=userRepository.findWithDetailsById(id).orElseThrow(()->new ResourceNotFoundException("User not found: "+id));
        if(actor.getRoles().stream().map(Role::getName).noneMatch("REGISTRAR"::equals)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Only the Registrar may manage grade scales."); return actor;}
    private GradeScaleResponse response(GradeScale s){return new GradeScaleResponse(s.getId(),s.getName(),s.isActive(),s.getEffectiveFrom(),s.getBands().stream().sorted(Comparator.comparing(GradeScaleBand::getMinMark)).map(b->new GradeScaleBandResponse(b.getId(),b.getMinMark(),b.getMaxMark(),b.getGrade(),b.getGradePoints(),b.isPass())).toList());}
}
