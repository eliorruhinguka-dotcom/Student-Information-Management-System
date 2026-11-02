package rw.university.sims.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rw.university.sims.dto.request.*;
import rw.university.sims.dto.response.*;
import rw.university.sims.entity.*;
import rw.university.sims.enums.RegistrationStatus;
import rw.university.sims.enums.ResultStatus;
import rw.university.sims.exception.ResourceConflictException;
import rw.university.sims.exception.ResourceNotFoundException;
import rw.university.sims.repository.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional
public class AssessmentService {
    private final AssessmentRepository assessmentRepository;
    private final AssessmentScoreRepository scoreRepository;
    private final CourseOfferingRepository offeringRepository;
    private final CourseRegistrationRepository registrationRepository;
    private final AcademicResultRepository resultRepository;
    private final UserAccountRepository userRepository;
    private final AuditService auditService;

    public AssessmentService(AssessmentRepository assessmentRepository, AssessmentScoreRepository scoreRepository,
            CourseOfferingRepository offeringRepository, CourseRegistrationRepository registrationRepository,
            AcademicResultRepository resultRepository, UserAccountRepository userRepository, AuditService auditService) {
        this.assessmentRepository = assessmentRepository; this.scoreRepository = scoreRepository;
        this.offeringRepository = offeringRepository; this.registrationRepository = registrationRepository;
        this.resultRepository = resultRepository; this.userRepository = userRepository; this.auditService = auditService;
    }

    public AssessmentResponse create(Long offeringId, AssessmentCreateRequest request, Long actorId, String ip) {
        CourseOffering offering = requireOwnedOffering(offeringId, actorId);
        ensureEditable(offeringId);
        if (assessmentRepository.existsByCourseOffering_IdAndNameIgnoreCase(offeringId, request.name().trim()))
            throw new ResourceConflictException("An assessment with this name already exists for the offering.");
        BigDecimal currentWeight = totalWeight(offeringId);
        if (currentWeight.add(request.weightPercent()).compareTo(new BigDecimal("100.00")) > 0)
            throw new ResourceConflictException("Assessment weights cannot exceed 100 percent.");
        UserAccount actor = requireUser(actorId);
        Assessment saved = assessmentRepository.saveAndFlush(new Assessment(offering, request.name().trim(),
                request.type(), request.maxMark(), request.weightPercent(), actor));
        auditService.record(actor, "ASSESSMENT_CREATED", "ASSESSMENT", saved.getId(), "SUCCESS", ip, null);
        return response(saved);
    }

    @Transactional(readOnly = true)
    public List<AssessmentResponse> list(Long offeringId, Long actorId) {
        CourseOffering offering = requireOffering(offeringId);
        enforceOfferingReadScope(offering, actorId);
        return assessmentRepository.findAllByCourseOffering_IdOrderByIdAsc(offeringId).stream()
                .map(this::response).toList();
    }

    public AssessmentResponse update(Long assessmentId, AssessmentUpdateRequest request, Long actorId, String ip) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + assessmentId));
        Long offeringId = assessment.getCourseOffering().getId();
        requireOwnedOffering(offeringId, actorId);
        ensureEditable(offeringId);
        if (assessmentRepository.existsByCourseOffering_IdAndNameIgnoreCaseAndIdNot(
                offeringId, request.name().trim(), assessmentId))
            throw new ResourceConflictException("Another assessment already uses this name.");
        BigDecimal otherWeights = totalWeight(offeringId).subtract(assessment.getWeightPercent());
        if (otherWeights.add(request.weightPercent()).compareTo(new BigDecimal("100.00")) > 0)
            throw new ResourceConflictException("Assessment weights cannot exceed 100 percent.");
        assessment.setName(request.name().trim()); assessment.setType(request.type());
        assessment.setMaxMark(request.maxMark()); assessment.setWeightPercent(request.weightPercent());
        Assessment saved = assessmentRepository.saveAndFlush(assessment);
        UserAccount actor = requireUser(actorId);
        auditService.record(actor, "ASSESSMENT_UPDATED", "ASSESSMENT", assessmentId, "SUCCESS", ip, null);
        return response(saved);
    }

    public List<AssessmentScoreResponse> updateScores(Long assessmentId, AssessmentScoresUpdateRequest request,
            Long actorId, String ip) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + assessmentId));
        Long offeringId = assessment.getCourseOffering().getId();
        requireOwnedOffering(offeringId, actorId);
        ensureEditable(offeringId);
        UserAccount actor = requireUser(actorId);
        for (AssessmentScoreEntry entry : request.scores()) {
            if (entry.mark().compareTo(assessment.getMaxMark()) > 0)
                throw new IllegalArgumentException("Mark cannot exceed assessment max mark for registration " + entry.courseRegistrationId());
            CourseRegistration registration = registrationRepository.findById(entry.courseRegistrationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Course registration not found: " + entry.courseRegistrationId()));
            if (!Objects.equals(registration.getCourseOffering().getId(), offeringId)
                    || registration.getStatus() != RegistrationStatus.REGISTERED)
                throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                        "Scores can only be entered for currently registered students in this offering.");
            AssessmentScore score = scoreRepository.findByAssessment_IdAndCourseRegistration_Id(
                    assessmentId, entry.courseRegistrationId())
                    .orElseGet(() -> new AssessmentScore(assessment, registration, entry.mark(), actor));
            score.setMark(entry.mark()); score.setEnteredBy(actor);
            scoreRepository.save(score);
        }
        auditService.record(actor, "ASSESSMENT_SCORES_ENTERED", "ASSESSMENT", assessmentId, "SUCCESS", ip, null);
        return scoreRepository.findAllByAssessment_IdOrderByCourseRegistration_IdAsc(assessmentId).stream()
                .map(this::scoreResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AssessmentScoreResponse> scores(Long assessmentId, Long actorId) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Assessment not found: " + assessmentId));
        enforceOfferingReadScope(assessment.getCourseOffering(), actorId);
        return scoreRepository.findAllByAssessment_IdOrderByCourseRegistration_IdAsc(assessmentId).stream()
                .map(this::scoreResponse).toList();
    }

    private void ensureEditable(Long offeringId) {
        if (resultRepository.countByCourseRegistration_CourseOffering_IdAndStatus(offeringId, ResultStatus.SUBMITTED) > 0
                || resultRepository.countByCourseRegistration_CourseOffering_IdAndStatus(offeringId, ResultStatus.PUBLISHED) > 0)
            throw new ResourceConflictException("Assessments are locked after any result has been submitted.");
    }
    private BigDecimal totalWeight(Long offeringId) {
        return assessmentRepository.findAllByCourseOffering_IdOrderByIdAsc(offeringId).stream()
                .map(Assessment::getWeightPercent).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    private CourseOffering requireOwnedOffering(Long offeringId, Long actorId) {
        CourseOffering offering = requireOffering(offeringId);
        UserAccount actor = requireUser(actorId);
        if (!roleNames(actor).contains("LECTURER") || !Objects.equals(offering.getLecturer().getId(), actorId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the assigned lecturer may manage assessments and marks.");
        return offering;
    }
    private void enforceOfferingReadScope(CourseOffering offering, Long actorId) {
        UserAccount actor = requireUser(actorId); Set<String> roles = roleNames(actor);
        if (roles.contains("REGISTRAR")) return;
        if (roles.contains("LECTURER") && Objects.equals(offering.getLecturer().getId(), actorId)) return;
        if (roles.contains("DEAN") && actor.getDepartment() != null
                && Objects.equals(actor.getDepartment().getId(), offering.getCourse().getDepartment().getId())) return;
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Offering is outside your permitted scope.");
    }
    private CourseOffering requireOffering(Long id) {
        return offeringRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course offering not found: " + id));
    }
    private UserAccount requireUser(Long id) {
        return userRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
    private Set<String> roleNames(UserAccount user) {
        return user.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet());
    }
    private AssessmentResponse response(Assessment a) {
        return new AssessmentResponse(a.getId(), a.getCourseOffering().getId(), a.getName(), a.getType(),
                a.getMaxMark(), a.getWeightPercent(), a.getCreatedAt());
    }
    private AssessmentScoreResponse scoreResponse(AssessmentScore s) {
        Student student = s.getCourseRegistration().getStudent();
        return new AssessmentScoreResponse(s.getId(), s.getAssessment().getId(), s.getCourseRegistration().getId(),
                student.getId(), student.getRegistrationNumber(), student.getFirstName(), student.getLastName(),
                s.getMark(), s.getEnteredBy().getId(), s.getEnteredAt(), s.getUpdatedAt());
    }
}
