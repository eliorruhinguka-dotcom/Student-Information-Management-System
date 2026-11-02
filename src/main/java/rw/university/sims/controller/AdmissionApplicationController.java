package rw.university.sims.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import rw.university.sims.dto.request.*;
import rw.university.sims.dto.response.*;
import rw.university.sims.enums.ApplicationStatus;
import rw.university.sims.service.AdmissionApplicationService;

@RestController
@RequestMapping("/api/v1/applications")
public class AdmissionApplicationController {
    private final AdmissionApplicationService service;
    public AdmissionApplicationController(AdmissionApplicationService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GenericResponse<ApplicationReceiptResponse> submit(
            @Valid @RequestBody AdmissionApplicationCreateRequest request, HttpServletRequest http) {
        return GenericResponse.success(201, "Application received successfully",
                service.submit(request, http.getRemoteAddr()));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('STUDENT_APPLICATION_REVIEW')")
    public GenericResponse<PageResponse<AdmissionApplicationResponse>> search(
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) Long programmeId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "submittedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return GenericResponse.success(200, "Applications retrieved successfully",
                service.search(status, programmeId, q, page, size, sortBy, direction));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('STUDENT_APPLICATION_REVIEW')")
    public GenericResponse<AdmissionApplicationResponse> get(@PathVariable Long id) {
        return GenericResponse.success(200, "Application retrieved successfully", service.get(id));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('STUDENT_APPLICATION_REVIEW')")
    public GenericResponse<StudentProfileResponse> approve(@PathVariable Long id,
            @Valid @RequestBody AdmissionApprovalRequest request,
            @AuthenticationPrincipal Jwt jwt, HttpServletRequest http) {
        return GenericResponse.success(201, "Application approved and student admitted",
                service.approve(id, request, Long.valueOf(jwt.getSubject()), http.getRemoteAddr()));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('STUDENT_APPLICATION_REVIEW')")
    public GenericResponse<AdmissionApplicationResponse> reject(@PathVariable Long id,
            @Valid @RequestBody AdmissionRejectRequest request,
            @AuthenticationPrincipal Jwt jwt, HttpServletRequest http) {
        return GenericResponse.success(200, "Application rejected",
                service.reject(id, request, Long.valueOf(jwt.getSubject()), http.getRemoteAddr()));
    }
}
