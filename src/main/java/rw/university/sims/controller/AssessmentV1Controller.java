package rw.university.sims.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import rw.university.sims.dto.request.*;
import rw.university.sims.dto.response.*;
import rw.university.sims.service.AssessmentService;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AssessmentV1Controller {
    private final AssessmentService service;
    public AssessmentV1Controller(AssessmentService service) { this.service = service; }

    @PostMapping("/course-offerings/{id}/assessments")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ASSESSMENT_MANAGE')")
    public GenericResponse<AssessmentResponse> create(@PathVariable Long id,
            @Valid @RequestBody AssessmentCreateRequest request,
            @AuthenticationPrincipal Jwt jwt, HttpServletRequest http) {
        return GenericResponse.success(201, "Assessment created successfully",
                service.create(id, request, Long.valueOf(jwt.getSubject()), http.getRemoteAddr()));
    }

    @GetMapping("/course-offerings/{id}/assessments")
    @PreAuthorize("hasAuthority('RESULT_READ_ALL')")
    public GenericResponse<List<AssessmentResponse>> list(@PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return GenericResponse.success(200, "Assessments retrieved successfully",
                service.list(id, Long.valueOf(jwt.getSubject())));
    }

    @PutMapping("/assessments/{id}")
    @PreAuthorize("hasAuthority('ASSESSMENT_MANAGE')")
    public GenericResponse<AssessmentResponse> update(@PathVariable Long id,
            @Valid @RequestBody AssessmentUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt, HttpServletRequest http) {
        return GenericResponse.success(200, "Assessment updated successfully",
                service.update(id, request, Long.valueOf(jwt.getSubject()), http.getRemoteAddr()));
    }

    @PutMapping("/assessments/{id}/scores")
    @PreAuthorize("hasAuthority('MARK_ENTER')")
    public GenericResponse<List<AssessmentScoreResponse>> updateScores(@PathVariable Long id,
            @Valid @RequestBody AssessmentScoresUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt, HttpServletRequest http) {
        return GenericResponse.success(200, "Assessment marks saved successfully",
                service.updateScores(id, request, Long.valueOf(jwt.getSubject()), http.getRemoteAddr()));
    }

    @GetMapping("/assessments/{id}/scores")
    @PreAuthorize("hasAuthority('RESULT_READ_ALL')")
    public GenericResponse<List<AssessmentScoreResponse>> scores(@PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {
        return GenericResponse.success(200, "Assessment marks retrieved successfully",
                service.scores(id, Long.valueOf(jwt.getSubject())));
    }
}
