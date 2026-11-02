package rw.university.sims.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import rw.university.sims.dto.request.ResultCorrectionRequest;
import rw.university.sims.dto.request.ResultReturnRequest;
import rw.university.sims.dto.response.*;
import rw.university.sims.service.ResultManagementService;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class ResultV1Controller {
    private final ResultManagementService service;
    public ResultV1Controller(ResultManagementService service) { this.service = service; }

    @PostMapping("/course-offerings/{id}/results/calculate")
    @PreAuthorize("hasAuthority('RESULT_SUBMIT')")
    public GenericResponse<List<AcademicResultResponse>> calculate(@PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt, HttpServletRequest http) {
        return GenericResponse.success(200, "Draft results calculated successfully",
                service.calculate(id, Long.valueOf(jwt.getSubject()), http.getRemoteAddr()));
    }

    @PostMapping("/course-offerings/{id}/results/submit")
    @PreAuthorize("hasAuthority('RESULT_SUBMIT')")
    public GenericResponse<List<AcademicResultResponse>> submit(@PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt, HttpServletRequest http) {
        return GenericResponse.success(200, "Results submitted for approval",
                service.submit(id, Long.valueOf(jwt.getSubject()), http.getRemoteAddr()));
    }

    @GetMapping("/course-offerings/{id}/results")
    @PreAuthorize("hasAuthority('RESULT_READ_ALL')")
    public GenericResponse<List<AcademicResultResponse>> offeringResults(@PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return GenericResponse.success(200, "Offering results retrieved successfully",
                service.offeringResults(id, Long.valueOf(jwt.getSubject())));
    }

    @PostMapping("/course-offerings/{id}/results/approve")
    @PreAuthorize("hasAuthority('RESULT_APPROVE')")
    public GenericResponse<List<AcademicResultResponse>> approve(@PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt, HttpServletRequest http) {
        return GenericResponse.success(200, "Results approved and published",
                service.approve(id, Long.valueOf(jwt.getSubject()), http.getRemoteAddr()));
    }

    @PostMapping("/course-offerings/{id}/results/return")
    @PreAuthorize("hasAuthority('RESULT_RETURN')")
    public GenericResponse<List<AcademicResultResponse>> returnToDraft(@PathVariable Long id,
            @Valid @RequestBody ResultReturnRequest request,
            @AuthenticationPrincipal Jwt jwt, HttpServletRequest http) {
        return GenericResponse.success(200, "Results returned to the lecturer for correction",
                service.returnToDraft(id, request, Long.valueOf(jwt.getSubject()), http.getRemoteAddr()));
    }

    @GetMapping("/results/mine")
    @PreAuthorize("hasAuthority('RESULT_READ_PUBLISHED')")
    public GenericResponse<List<AcademicResultResponse>> mine(@AuthenticationPrincipal Jwt jwt) {
        return GenericResponse.success(200, "Your published results retrieved successfully",
                service.myPublishedResults(Long.valueOf(jwt.getSubject())));
    }

    @PostMapping("/results/{id}/corrections")
    @PreAuthorize("hasAuthority('RESULT_CORRECT')")
    public GenericResponse<ResultCorrectionResponse> correct(@PathVariable Long id,
            @Valid @RequestBody ResultCorrectionRequest request,
            @AuthenticationPrincipal Jwt jwt, HttpServletRequest http) {
        return GenericResponse.success(201, "Published result corrected successfully",
                service.correct(id, request, Long.valueOf(jwt.getSubject()), http.getRemoteAddr()));
    }
}
