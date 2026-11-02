package rw.university.sims.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rw.university.sims.dto.response.*;
import rw.university.sims.entity.CourseOffering;
import rw.university.sims.entity.Role;
import rw.university.sims.entity.UserAccount;
import rw.university.sims.exception.ResourceNotFoundException;
import rw.university.sims.repository.CourseOfferingRepository;
import rw.university.sims.repository.ReportQueryRepository;
import rw.university.sims.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class ReportingService {
    private final ReportQueryRepository reports;
    private final UserAccountRepository userRepository;
    private final CourseOfferingRepository offeringRepository;

    public ReportingService(ReportQueryRepository reports, UserAccountRepository userRepository,
            CourseOfferingRepository offeringRepository) {
        this.reports = reports;
        this.userRepository = userRepository;
        this.offeringRepository = offeringRepository;
    }

    public List<EnrolmentByProgrammeRow> enrolmentByProgramme(Long semesterId, Long actorId) {
        requirePositiveId(semesterId, "semesterId");
        UserAccount actor = requireUser(actorId);
        Set<String> roles = roleNames(actor);
        Long departmentScope = null;
        Long lecturerScope = null;
        if (roles.contains("REGISTRAR")) {
            // University-wide view.
        } else if (roles.contains("LECTURER")) {
            lecturerScope = actorId;
        } else if (roles.contains("DEAN")) {
            departmentScope = requireDepartmentId(actor);
        } else {
            throw forbidden("You do not have academic reporting access.");
        }
        return reports.enrolmentByProgramme(semesterId, departmentScope, lecturerScope);
    }

    public List<OfferingCapacityRow> offeringCapacity(Long semesterId, Long requestedDepartmentId,
            Long actorId) {
        requirePositiveId(semesterId, "semesterId");
        UserAccount actor = requireUser(actorId);
        Set<String> roles = roleNames(actor);
        Long departmentScope = requestedDepartmentId;
        Long lecturerScope = null;
        if (roles.contains("REGISTRAR")) {
            // Registrar may filter any department or view all.
        } else if (roles.contains("LECTURER")) {
            Long ownDepartment = requireDepartmentId(actor);
            if (requestedDepartmentId != null && !Objects.equals(requestedDepartmentId, ownDepartment)) {
                throw forbidden("Lecturers may only view capacity in their own department and offerings.");
            }
            departmentScope = ownDepartment;
            lecturerScope = actorId;
        } else if (roles.contains("DEAN")) {
            Long ownDepartment = requireDepartmentId(actor);
            if (requestedDepartmentId != null && !Objects.equals(requestedDepartmentId, ownDepartment)) {
                throw forbidden("Deans may only view capacity in their own department.");
            }
            departmentScope = ownDepartment;
        } else {
            throw forbidden("You do not have academic reporting access.");
        }
        return reports.offeringCapacity(semesterId, departmentScope, lecturerScope);
    }

    public OutstandingFeesReportResponse outstandingFees(Long semesterId, Long actorId) {
        requirePositiveId(semesterId, "semesterId");
        UserAccount actor = requireUser(actorId);
        Set<String> roles = roleNames(actor);
        if (!roles.contains("REGISTRAR") && !roles.contains("FINANCE_OFFICER")) {
            throw forbidden("Only Registrars and Finance Officers may view the outstanding-fees report.");
        }
        List<OutstandingFeesStudentRow> rows = reports.outstandingFees(semesterId);
        Map<String, BigDecimal[]> totals = new TreeMap<>();
        for (OutstandingFeesStudentRow row : rows) {
            BigDecimal[] values = totals.computeIfAbsent(row.currency(), ignored ->
                    new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
            values[0] = values[0].add(row.totalDue());
            values[1] = values[1].add(row.totalPaid());
        }
        List<OutstandingFeesCurrencyTotal> summary = totals.entrySet().stream()
                .map(entry -> new OutstandingFeesCurrencyTotal(
                        entry.getKey(), entry.getValue()[0], entry.getValue()[1],
                        entry.getValue()[0].subtract(entry.getValue()[1])))
                .toList();
        return new OutstandingFeesReportResponse(semesterId, rows, summary);
    }

    public GradeDistributionReportResponse gradeDistribution(Long offeringId, Long actorId) {
        requirePositiveId(offeringId, "offeringId");
        UserAccount actor = requireUser(actorId);
        CourseOffering offering = offeringRepository.findDetailedById(offeringId)
                .orElseThrow(() -> new ResourceNotFoundException("Course offering not found: " + offeringId));
        Set<String> roles = roleNames(actor);
        boolean allowed = roles.contains("REGISTRAR")
                || (roles.contains("LECTURER") && Objects.equals(offering.getLecturer().getId(), actorId))
                || (roles.contains("DEAN") && actor.getDepartment() != null
                    && Objects.equals(actor.getDepartment().getId(), offering.getCourse().getDepartment().getId()));
        if (!allowed) throw forbidden("This offering is outside your academic reporting scope.");
        List<GradeDistributionRow> distribution = reports.gradeDistribution(offeringId);
        long total = distribution.stream().mapToLong(GradeDistributionRow::count).sum();
        return new GradeDistributionReportResponse(offeringId, total, distribution);
    }

    private UserAccount requireUser(Long id) {
        return userRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    private Long requireDepartmentId(UserAccount actor) {
        if (actor.getDepartment() == null || actor.getDepartment().getId() == null) {
            throw forbidden("Your account has no department scope configured.");
        }
        return actor.getDepartment().getId();
    }

    private Set<String> roleNames(UserAccount actor) {
        Set<String> roles = new HashSet<>();
        for (Role role : actor.getRoles()) roles.add(role.getName());
        return roles;
    }

    private void requirePositiveId(Long id, String field) {
        if (id == null || id <= 0) throw new IllegalArgumentException(field + " must be a positive number.");
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(HttpStatus.FORBIDDEN, message);
    }
}
