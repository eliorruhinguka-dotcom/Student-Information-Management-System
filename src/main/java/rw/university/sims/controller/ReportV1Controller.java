package rw.university.sims.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import rw.university.sims.dto.response.*;
import rw.university.sims.service.ReportingService;
import java.util.List;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportV1Controller {
    private final ReportingService service;

    public ReportV1Controller(ReportingService service) {
        this.service = service;
    }

    @GetMapping("/enrolment-by-programme")
    @PreAuthorize("hasAuthority('REPORT_ACADEMIC_VIEW')")
    public GenericResponse<List<EnrolmentByProgrammeRow>> enrolmentByProgramme(
            @RequestParam Long semesterId, @AuthenticationPrincipal Jwt jwt) {
        return GenericResponse.success(200, "Enrolment-by-programme report retrieved successfully",
                service.enrolmentByProgramme(semesterId, Long.valueOf(jwt.getSubject())));
    }

    @GetMapping("/offering-capacity")
    @PreAuthorize("hasAuthority('REPORT_ACADEMIC_VIEW')")
    public GenericResponse<List<OfferingCapacityRow>> offeringCapacity(
            @RequestParam Long semesterId, @RequestParam(required = false) Long departmentId,
            @AuthenticationPrincipal Jwt jwt) {
        return GenericResponse.success(200, "Offering-capacity report retrieved successfully",
                service.offeringCapacity(semesterId, departmentId, Long.valueOf(jwt.getSubject())));
    }

    @GetMapping("/outstanding-fees")
    @PreAuthorize("hasAuthority('REPORT_FINANCE_VIEW')")
    public GenericResponse<OutstandingFeesReportResponse> outstandingFees(
            @RequestParam Long semesterId, @AuthenticationPrincipal Jwt jwt) {
        return GenericResponse.success(200, "Outstanding-fees report retrieved successfully",
                service.outstandingFees(semesterId, Long.valueOf(jwt.getSubject())));
    }

    @GetMapping("/grade-distribution/{offeringId}")
    @PreAuthorize("hasAuthority('REPORT_ACADEMIC_VIEW')")
    public GenericResponse<GradeDistributionReportResponse> gradeDistribution(
            @PathVariable Long offeringId, @AuthenticationPrincipal Jwt jwt) {
        return GenericResponse.success(200, "Grade-distribution report retrieved successfully",
                service.gradeDistribution(offeringId, Long.valueOf(jwt.getSubject())));
    }
}
