package rw.university.sims.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import rw.university.sims.dto.request.StudentUpdateRequest;
import rw.university.sims.dto.response.GenericResponse;
import rw.university.sims.dto.response.StudentProfileResponse;
import rw.university.sims.dto.response.StudentStatusHistoryResponse;
import rw.university.sims.service.StudentProfileService;
import java.util.List;

@RestController
@RequestMapping("/api/v1/students")
public class StudentV1Controller {
    private final StudentProfileService service;
    public StudentV1Controller(StudentProfileService service) { this.service = service; }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('STUDENT_UPDATE')")
    public GenericResponse<StudentProfileResponse> update(@PathVariable Long id,
            @Valid @RequestBody StudentUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt, HttpServletRequest http) {
        return GenericResponse.success(200, "Student profile updated successfully",
                service.update(id, request, Long.valueOf(jwt.getSubject()), http.getRemoteAddr()));
    }

    @GetMapping("/{id}/status-history")
    @PreAuthorize("hasAuthority('STUDENT_READ')")
    public GenericResponse<List<StudentStatusHistoryResponse>> history(@PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return GenericResponse.success(200, "Student status history retrieved successfully",
                service.statusHistory(id, Long.valueOf(jwt.getSubject())));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('STUDENT_READ')")
    public GenericResponse<StudentProfileResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return GenericResponse.success(200, "Your student profile retrieved successfully",
                service.getMyProfile(Long.valueOf(jwt.getSubject())));
    }
}
