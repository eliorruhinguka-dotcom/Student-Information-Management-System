package rw.university.sims.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rw.university.sims.dto.request.ResultCorrectionRequest;
import rw.university.sims.dto.response.ResultCorrectionHistoryResponse;
import rw.university.sims.entity.*;
import rw.university.sims.enums.ResultStatus;
import rw.university.sims.exception.ResourceConflictException;
import rw.university.sims.exception.ResourceNotFoundException;
import rw.university.sims.repository.AcademicResultRepository;
import rw.university.sims.repository.ResultCorrectionRepository;
import rw.university.sims.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ResultCorrectionExtensionService {
    private final AcademicResultRepository resultRepository;
    private final ResultCorrectionRepository correctionRepository;
    private final UserAccountRepository userRepository;
    private final AuditService auditService;

    public ResultCorrectionExtensionService(AcademicResultRepository resultRepository,
            ResultCorrectionRepository correctionRepository, UserAccountRepository userRepository,
            AuditService auditService) {
        this.resultRepository = resultRepository;
        this.correctionRepository = correctionRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<ResultCorrectionHistoryResponse> history(Long resultId, Long actorId) {
        AcademicResult result = requireResult(resultId);
        enforceReadScope(result, actorId);
        return correctionRepository.findAllByResult_IdOrderByCorrectedAtDesc(resultId)
                .stream().map(this::toResponse).toList();
    }

    public ResultCorrectionHistoryResponse request(Long resultId, ResultCorrectionRequest request,
            Long actorId, String ipAddress) {
        AcademicResult result = resultRepository.findByIdForUpdate(resultId)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found: " + resultId));
        UserAccount lecturer = requireUser(actorId);
        CourseOffering offering = result.getCourseRegistration().getCourseOffering();
        if (!roleNames(lecturer).contains("LECTURER")
                || !Objects.equals(offering.getLecturer().getId(), actorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Only the lecturer assigned to this offering may request a correction.");
        }
        if (result.getStatus() != ResultStatus.PUBLISHED) {
            throw new ResourceConflictException("Only PUBLISHED results may have correction requests.");
        }
        if (result.getFinalMark() == null || result.getGradeScale() == null) {
            throw new ResourceConflictException("The published result has no final mark or grade scale.");
        }
        BigDecimal proposedMark = request.correctedMark();
        if (result.getFinalMark().compareTo(proposedMark) == 0) {
            throw new IllegalArgumentException("The requested corrected mark must differ from the current mark.");
        }
        GradeScaleBand proposedBand = result.getGradeScale().getBands().stream()
                .filter(b -> proposedMark.compareTo(b.getMinMark()) >= 0
                        && proposedMark.compareTo(b.getMaxMark()) <= 0)
                .findFirst()
                .orElseThrow(() -> new ResourceConflictException(
                        "No grade band covers the proposed mark " + proposedMark + "."));

        ResultCorrection pending = new ResultCorrection(
                result, result.getFinalMark(), proposedMark, result.getGrade(), proposedBand.getGrade(),
                request.reason().trim(), lecturer, null, "REQUESTED");
        correctionRepository.saveAndFlush(pending);
        auditService.record(lecturer, "RESULT_CORRECTION_REQUESTED", "RESULT",
                resultId, "SUCCESS", ipAddress, null);
        return toResponse(pending);
    }

    private AcademicResult requireResult(Long id) {
        return resultRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Result not found: " + id));
    }

    private UserAccount requireUser(Long id) {
        return userRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private void enforceReadScope(AcademicResult result, Long actorId) {
        UserAccount actor = requireUser(actorId);
        Set<String> roles = roleNames(actor);
        CourseOffering offering = result.getCourseRegistration().getCourseOffering();
        if (roles.contains("REGISTRAR")) return;
        if (roles.contains("LECTURER") && Objects.equals(offering.getLecturer().getId(), actorId)) return;
        if (roles.contains("DEAN") && actor.getDepartment() != null
                && Objects.equals(actor.getDepartment().getId(), offering.getCourse().getDepartment().getId())) return;
        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Correction history is outside your permitted scope.");
    }

    private Set<String> roleNames(UserAccount user) {
        return user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
    }

    private ResultCorrectionHistoryResponse toResponse(ResultCorrection correction) {
        UserAccount requester = correction.getRequestedBy();
        UserAccount authoriser = correction.getAuthorisedBy();
        return new ResultCorrectionHistoryResponse(
                correction.getId(), correction.getResult().getId(), correction.getOriginalMark(),
                correction.getCorrectedMark(), correction.getOriginalGrade(), correction.getCorrectedGrade(),
                correction.getReason(), correction.getRequestStatus(),
                requester == null ? null : requester.getId(), requester == null ? null : requester.getFullName(),
                authoriser == null ? null : authoriser.getId(), authoriser == null ? null : authoriser.getFullName(),
                correction.getCorrectedAt());
    }
}
