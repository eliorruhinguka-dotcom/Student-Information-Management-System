package rw.university.sims.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import rw.university.sims.dto.request.CoursePrerequisiteRequest;
import rw.university.sims.dto.response.CourseResponse;
import rw.university.sims.dto.response.GenericResponse;
import rw.university.sims.service.CoursePrerequisiteService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/courses")
public class CoursePrerequisiteController {

    private final CoursePrerequisiteService prerequisiteService;

    public CoursePrerequisiteController(
            CoursePrerequisiteService prerequisiteService) {
        this.prerequisiteService = prerequisiteService;
    }

    // GET /api/v1/courses/{id}/prerequisites
    @GetMapping("/{id}/prerequisites")
    public GenericResponse<List<CourseResponse>> getPrerequisites(
            @PathVariable Long id) {

        List<CourseResponse> prerequisites =
                prerequisiteService.getPrerequisites(id);

        return GenericResponse.success(
                200,
                "Course prerequisites retrieved successfully",
                prerequisites
        );
    }

    // POST /api/v1/courses/{id}/prerequisites
    @PostMapping("/{id}/prerequisites")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CATALOGUE_MANAGE')")
    public GenericResponse<CourseResponse> addPrerequisite(
            @PathVariable Long id,
            @Valid @RequestBody CoursePrerequisiteRequest request) {

        CourseResponse prerequisite =
                prerequisiteService.addPrerequisite(id, request);

        return GenericResponse.success(
                201,
                "Course prerequisite added successfully",
                prerequisite
        );
    }

    // DELETE /api/v1/courses/{id}/prerequisites/{prerequisiteId}
    @DeleteMapping("/{id}/prerequisites/{prerequisiteId}")
    @PreAuthorize("hasAuthority('CATALOGUE_MANAGE')")
    public GenericResponse<Void> removePrerequisite(
            @PathVariable Long id,
            @PathVariable Long prerequisiteId,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {

        prerequisiteService.removePrerequisite(
                id,
                prerequisiteId,
                Long.valueOf(jwt.getSubject()),
                httpRequest.getRemoteAddr()
        );

        return GenericResponse.success(
                200,
                "Course prerequisite removed successfully",
                null
        );
    }
}