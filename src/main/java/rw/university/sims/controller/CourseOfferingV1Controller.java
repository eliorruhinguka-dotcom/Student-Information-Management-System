package rw.university.sims.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import rw.university.sims.dto.request.OfferingCreateRequest;
import rw.university.sims.dto.request.OfferingStatusRequest;
import rw.university.sims.dto.request.OfferingUpdateRequest;
import rw.university.sims.dto.response.CourseOfferingResponse;
import rw.university.sims.dto.response.GenericResponse;
import rw.university.sims.dto.response.OfferingRegistrationResponse;
import rw.university.sims.dto.response.PageResponse;
import rw.university.sims.service.CourseOfferingManagementService;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/course-offerings")
public class CourseOfferingV1Controller {

    private static final Set<String> SORTABLE_FIELDS = Set.of(
            "id",
            "section",
            "status",
            "capacity",
            "registeredCount",
            "credits",
            "course.courseCode",
            "registeredAt"
    );

    private final CourseOfferingManagementService offeringService;

    public CourseOfferingV1Controller(
            CourseOfferingManagementService offeringService) {
        this.offeringService = offeringService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('OFFERING_MANAGE')")
    public GenericResponse<CourseOfferingResponse> createOffering(
            @Valid @RequestBody OfferingCreateRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {

        CourseOfferingResponse response = offeringService.createOffering(
                request,
                Long.valueOf(jwt.getSubject()),
                httpRequest.getRemoteAddr()
        );

        return GenericResponse.success(
                201,
                "Course offering created successfully",
                response
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('OFFERING_READ')")
    public GenericResponse<PageResponse<CourseOfferingResponse>> getOfferings(
            @RequestParam(required = false) Long semesterId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long lecturerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @AuthenticationPrincipal Jwt jwt) {

        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        PageResponse<CourseOfferingResponse> response =
                offeringService.searchOfferings(
                        semesterId,
                        courseId,
                        departmentId,
                        lecturerId,
                        pageable,
                        Long.valueOf(jwt.getSubject())
                );

        return GenericResponse.success(
                200,
                "Course offerings retrieved successfully",
                response
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('OFFERING_READ')")
    public GenericResponse<CourseOfferingResponse> getOffering(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        return GenericResponse.success(
                200,
                "Course offering retrieved successfully",
                offeringService.getOffering(
                        id,
                        Long.valueOf(jwt.getSubject())
                )
        );
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAuthority('OFFERING_READ')")
    public GenericResponse<PageResponse<CourseOfferingResponse>> getMyOfferings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt) {

        Pageable pageable = buildPageable(
                page, size, "id", "desc");

        return GenericResponse.success(
                200,
                "Your course offerings retrieved successfully",
                offeringService.getMyOfferings(
                        Long.valueOf(jwt.getSubject()),
                        pageable
                )
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('OFFERING_MANAGE')")
    public GenericResponse<CourseOfferingResponse> updateOffering(
            @PathVariable Long id,
            @Valid @RequestBody OfferingUpdateRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {

        return GenericResponse.success(
                200,
                "Course offering updated successfully",
                offeringService.updateOffering(
                        id,
                        request,
                        Long.valueOf(jwt.getSubject()),
                        httpRequest.getRemoteAddr()
                )
        );
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('OFFERING_MANAGE')")
    public GenericResponse<CourseOfferingResponse> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody OfferingStatusRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest httpRequest) {

        return GenericResponse.success(
                200,
                "Course offering status updated successfully",
                offeringService.changeOfferingStatus(
                        id,
                        request,
                        Long.valueOf(jwt.getSubject()),
                        httpRequest.getRemoteAddr()
                )
        );
    }

    @GetMapping("/{id}/registrations")
    @PreAuthorize("hasAuthority('REGISTRATION_READ')")
    public GenericResponse<PageResponse<OfferingRegistrationResponse>>
            getOfferingRegistrations(
                    @PathVariable Long id,
                    @RequestParam(defaultValue = "0") int page,
                    @RequestParam(defaultValue = "20") int size,
                    @AuthenticationPrincipal Jwt jwt) {

        Pageable pageable = buildPageable(
                page, size, "registeredAt", "desc");

        return GenericResponse.success(
                200,
                "Offering registrations retrieved successfully",
                offeringService.getOfferingRegistrations(
                        id,
                        Long.valueOf(jwt.getSubject()),
                        pageable
                )
        );
    }

    private Pageable buildPageable(
            int page,
            int size,
            String sortBy,
            String sortDir) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100");
        }

        if (!SORTABLE_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException(
                    "Unsupported sort field: " + sortBy);
        }

        Sort.Direction direction;

        if ("asc".equalsIgnoreCase(sortDir)) {
            direction = Sort.Direction.ASC;
        } else if ("desc".equalsIgnoreCase(sortDir)) {
            direction = Sort.Direction.DESC;
        } else {
            throw new IllegalArgumentException(
                    "Sort direction must be asc or desc");
        }

        return PageRequest.of(
                page,
                size,
                Sort.by(direction, sortBy)
        );
    }
}