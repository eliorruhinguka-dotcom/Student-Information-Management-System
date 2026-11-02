package rw.university.sims.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rw.university.sims.dto.request.ResultCorrectionRequest;
import rw.university.sims.dto.request.ResultReturnRequest;
import rw.university.sims.dto.response.AcademicResultResponse;
import rw.university.sims.dto.response.ResultCorrectionResponse;
import rw.university.sims.entity.*;
import rw.university.sims.enums.OfferingStatus;
import rw.university.sims.enums.RegistrationStatus;
import rw.university.sims.enums.ResultStatus;
import rw.university.sims.exception.ResourceConflictException;
import rw.university.sims.exception.ResourceNotFoundException;
import rw.university.sims.repository.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;

@Service
@Transactional
public class ResultManagementService {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100.00");
    private final CourseOfferingRepository offeringRepository;
    private final CourseRegistrationRepository registrationRepository;
    private final AssessmentRepository assessmentRepository;
    private final AssessmentScoreRepository scoreRepository;
    private final GradeScaleRepository gradeScaleRepository;
    private final AcademicResultRepository resultRepository;
    private final ResultCorrectionRepository correctionRepository;
    private final UserAccountRepository userRepository;
    private final AuditService auditService;

    public ResultManagementService(CourseOfferingRepository offeringRepository,
            CourseRegistrationRepository registrationRepository, AssessmentRepository assessmentRepository,
            AssessmentScoreRepository scoreRepository, GradeScaleRepository gradeScaleRepository,
            AcademicResultRepository resultRepository, ResultCorrectionRepository correctionRepository,
            UserAccountRepository userRepository, AuditService auditService) {
        this.offeringRepository = offeringRepository; this.registrationRepository = registrationRepository;
        this.assessmentRepository = assessmentRepository; this.scoreRepository = scoreRepository;
        this.gradeScaleRepository = gradeScaleRepository; this.resultRepository = resultRepository;
        this.correctionRepository = correctionRepository; this.userRepository = userRepository;
        this.auditService = auditService;
    }

    public List<AcademicResultResponse> calculate(Long offeringId, Long actorId, String ip) {
        CourseOffering offering = requireOffering(offeringId);
        UserAccount actor = requireAssignedLecturer(offering, actorId);
        if (offering.getStatus() == OfferingStatus.CANCELLED)
            throw new ResourceConflictException("Results cannot be calculated for a cancelled offering.");
        List<Assessment> assessments = assessmentRepository.findAllByCourseOffering_IdOrderByIdAsc(offeringId);
        if (assessments.isEmpty()) throw new ResourceConflictException("Create assessments before calculating results.");
        BigDecimal totalWeight = assessments.stream().map(Assessment::getWeightPercent)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalWeight.compareTo(ONE_HUNDRED) != 0)
            throw new ResourceConflictException("Assessment weights must total exactly 100 percent.");
        GradeScale scale = gradeScaleRepository.findByActiveTrue()
                .orElseThrow(() -> new ResourceConflictException(
                        "No active grade scale is configured. Configure the university grading scale before calculating results."));
        List<CourseRegistration> registrations = registrationRepository.findAllByCourseOffering_IdAndStatus(
                offeringId, RegistrationStatus.REGISTERED);
        if (registrations.isEmpty()) throw new ResourceConflictException("There are no registered students in this offering.");

        List<AcademicResultResponse> output = new ArrayList<>();
        for (CourseRegistration registration : registrations) {
            BigDecimal finalMark = BigDecimal.ZERO;
            for (Assessment assessment : assessments) {
                AssessmentScore score = scoreRepository.findByAssessment_IdAndCourseRegistration_Id(
                        assessment.getId(), registration.getId())
                        .orElseThrow(() -> new ResourceConflictException("A mark is missing for registration " + registration.getId()));
                if (score.getMark() == null)
                    throw new ResourceConflictException("A mark is missing for registration " + registration.getId());
                BigDecimal weighted = score.getMark().divide(assessment.getMaxMark(), 8, RoundingMode.HALF_UP)
                        .multiply(assessment.getWeightPercent());
                finalMark = finalMark.add(weighted);
            }
            finalMark = finalMark.setScale(2, RoundingMode.HALF_UP);
            GradeScaleBand band = findBand(scale, finalMark);
            AcademicResult result = resultRepository.findByCourseRegistration_Id(registration.getId())
                    .orElseGet(() -> new AcademicResult(registration));
            if (result.getStatus() != ResultStatus.DRAFT)
                throw new ResourceConflictException("A result has already been submitted or published for registration " + registration.getId());
            result.setFinalMark(finalMark); result.setGrade(band.getGrade()); result.setPassed(band.isPass());
            result.setGradeScale(scale);
            output.add(toResponse(resultRepository.saveAndFlush(result)));
        }
        auditService.record(actor, "RESULTS_CALCULATED", "COURSE_OFFERING", offeringId, "SUCCESS", ip, null);
        return output;
    }

    public List<AcademicResultResponse> submit(Long offeringId, Long actorId, String ip) {
        CourseOffering offering = requireOffering(offeringId);
        UserAccount actor = requireAssignedLecturer(offering, actorId);
        List<CourseRegistration> registrations = registrationRepository.findAllByCourseOffering_IdAndStatus(
                offeringId, RegistrationStatus.REGISTERED);
        List<AcademicResult> results = resultRepository.findAllByCourseRegistration_CourseOffering_IdOrderByIdAsc(offeringId);
        if (registrations.isEmpty() || results.size() != registrations.size())
            throw new ResourceConflictException("Calculate a complete result for every registered student before submission.");
        OffsetDateTime now = OffsetDateTime.now();
        for (AcademicResult result : results) {
            if (result.getStatus() != ResultStatus.DRAFT || result.getFinalMark() == null || result.getGrade() == null)
                throw new ResourceConflictException("Every result must be complete and in DRAFT status before submission.");
            result.setStatus(ResultStatus.SUBMITTED); result.setSubmittedBy(actor); result.setSubmittedAt(now);
            resultRepository.save(result);
        }
        auditService.record(actor, "RESULTS_SUBMITTED", "COURSE_OFFERING", offeringId, "SUCCESS", ip, null);
        return results.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AcademicResultResponse> offeringResults(Long offeringId, Long actorId) {
        CourseOffering offering = requireOffering(offeringId);
        enforceResultReadScope(offering, actorId);
        return resultRepository.findAllByCourseRegistration_CourseOffering_IdOrderByIdAsc(offeringId)
                .stream().map(this::toResponse).toList();
    }

    public List<AcademicResultResponse> approve(Long offeringId, Long actorId, String ip) {
        CourseOffering offering = requireOffering(offeringId);
        UserAccount dean = requireDeanForOffering(offering, actorId);
        List<AcademicResult> results = resultRepository.findAllByCourseRegistration_CourseOffering_IdAndStatus(
                offeringId, ResultStatus.SUBMITTED);
        if (results.isEmpty()) throw new ResourceConflictException("There are no SUBMITTED results to approve.");
        OffsetDateTime now = OffsetDateTime.now();
        for (AcademicResult result : results) {
            result.setStatus(ResultStatus.PUBLISHED); result.setApprovedBy(dean); result.setApprovedAt(now);
            resultRepository.save(result);
        }
        auditService.record(dean, "RESULTS_APPROVED", "COURSE_OFFERING", offeringId, "SUCCESS", ip, null);
        return results.stream().map(this::toResponse).toList();
    }

    public List<AcademicResultResponse> returnToDraft(Long offeringId, ResultReturnRequest request,
            Long actorId, String ip) {
        CourseOffering offering = requireOffering(offeringId);
        UserAccount dean = requireDeanForOffering(offering, actorId);
        List<AcademicResult> results = resultRepository.findAllByCourseRegistration_CourseOffering_IdAndStatus(
                offeringId, ResultStatus.SUBMITTED);
        if (results.isEmpty()) throw new ResourceConflictException("There are no SUBMITTED results to return.");
        for (AcademicResult result : results) {
            result.setStatus(ResultStatus.DRAFT); result.setSubmittedBy(null); result.setSubmittedAt(null);
            result.setApprovedBy(null); result.setApprovedAt(null);
            resultRepository.save(result);
        }
        auditService.recordWithValues(dean, "RESULTS_RETURNED", "COURSE_OFFERING", offeringId,
                "SUCCESS", ip, null, Map.of("status", "SUBMITTED"),
                Map.of("status", "DRAFT", "reason", request.reason().trim()));
        return results.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<AcademicResultResponse> myPublishedResults(Long actorId) {
        UserAccount actor = requireUser(actorId);
        if (!roleNames(actor).contains("STUDENT") || actor.getStudentLink() == null
                || actor.getStudentLink().getStudent() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This endpoint is available to linked student accounts only.");
        Long studentId = actor.getStudentLink().getStudent().getId();
        return resultRepository.findAllByCourseRegistration_Student_IdAndStatusOrderByIdDesc(
                studentId, ResultStatus.PUBLISHED).stream().map(this::toResponse).toList();
    }

    public ResultCorrectionResponse correct(Long resultId, ResultCorrectionRequest request,
            Long actorId, String ip) {
        AcademicResult result = resultRepository.findByIdForUpdate(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found: " + resultId));
        UserAccount dean = requireDeanForOffering(result.getCourseRegistration().getCourseOffering(), actorId);
        if (result.getStatus() != ResultStatus.PUBLISHED)
            throw new ResourceConflictException("Only PUBLISHED results can be corrected.");
        GradeScale scale = result.getGradeScale();
        GradeScaleBand band = findBand(scale, request.correctedMark());
        BigDecimal originalMark = result.getFinalMark(); String originalGrade = result.getGrade();
        if (originalMark.compareTo(request.correctedMark()) == 0)
            throw new IllegalArgumentException("The corrected mark must be different from the original mark.");
        ResultCorrection correction = new ResultCorrection(result, originalMark, request.correctedMark(),
                originalGrade, band.getGrade(), request.reason().trim(), null, dean);
        correctionRepository.saveAndFlush(correction);
        result.setFinalMark(request.correctedMark()); result.setGrade(band.getGrade()); result.setPassed(band.isPass());
        resultRepository.saveAndFlush(result);
        auditService.record(dean, "RESULT_CORRECTED", "RESULT", resultId, "SUCCESS", ip, null);
        return new ResultCorrectionResponse(correction.getId(), resultId, originalMark, request.correctedMark(),
                originalGrade, band.getGrade(), request.reason().trim(), dean.getId(), dean.getFullName(), correction.getCorrectedAt());
    }

    private GradeScaleBand findBand(GradeScale scale, BigDecimal mark) {
        return scale.getBands().stream().filter(b -> mark.compareTo(b.getMinMark()) >= 0
                        && mark.compareTo(b.getMaxMark()) <= 0).findFirst()
                .orElseThrow(() -> new ResourceConflictException("No grade band covers mark " + mark + "."));
    }
    private CourseOffering requireOffering(Long id) {
        return offeringRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Course offering not found: " + id));
    }
    private UserAccount requireAssignedLecturer(CourseOffering offering, Long userId) {
        UserAccount actor = requireUser(userId);
        if (!roleNames(actor).contains("LECTURER") || !Objects.equals(offering.getLecturer().getId(), userId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the assigned lecturer may submit results for this offering.");
        return actor;
    }
    private UserAccount requireDeanForOffering(CourseOffering offering, Long userId) {
        UserAccount actor = requireUser(userId);
        if (!roleNames(actor).contains("DEAN") || actor.getDepartment() == null
                || !Objects.equals(actor.getDepartment().getId(), offering.getCourse().getDepartment().getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the Dean of the offering's department may perform this action.");
        return actor;
    }
    private void enforceResultReadScope(CourseOffering offering, Long userId) {
        UserAccount actor = requireUser(userId); Set<String> roles = roleNames(actor);
        if (roles.contains("REGISTRAR")) return;
        if (roles.contains("LECTURER") && Objects.equals(offering.getLecturer().getId(), userId)) return;
        if (roles.contains("DEAN") && actor.getDepartment() != null
                && Objects.equals(actor.getDepartment().getId(), offering.getCourse().getDepartment().getId())) return;
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Results are outside your permitted scope.");
    }
    private UserAccount requireUser(Long id) {
        return userRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
    private Set<String> roleNames(UserAccount actor) {
        return actor.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet());
    }
    private AcademicResultResponse toResponse(AcademicResult r) {
        CourseRegistration cr = r.getCourseRegistration(); Student s = cr.getStudent();
        CourseOffering o = cr.getCourseOffering(); Course c = o.getCourse();
        return new AcademicResultResponse(r.getId(), cr.getId(), s.getId(), s.getRegistrationNumber(),
                s.getFirstName(), s.getLastName(), o.getId(), c.getCourseCode(), c.getTitle(),
                r.getFinalMark(), r.getGrade(), r.getPassed(), r.getGradeScale() == null ? null : r.getGradeScale().getId(),
                r.getStatus(), r.getSubmittedAt(), r.getApprovedAt(), r.getVersion());
    }
}
