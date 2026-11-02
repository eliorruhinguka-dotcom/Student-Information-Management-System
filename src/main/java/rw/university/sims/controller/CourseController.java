package rw.university.sims.controller;

import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import rw.university.sims.dto.request.CourseRequest;
import rw.university.sims.dto.request.CourseUpdateRequest;
import rw.university.sims.dto.response.CourseResponse;
import rw.university.sims.dto.response.GenericResponse;
import rw.university.sims.dto.response.PageResponse;
import rw.university.sims.service.CourseService;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private static final Set<String> SORTABLE_FIELDS =
            Set.of("id", "courseCode", "title", "credits", "active");

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    // POST /api/v1/courses
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CATALOGUE_MANAGE')")
    public GenericResponse<CourseResponse> createCourse(
            @Valid @RequestBody CourseRequest request) {

        CourseResponse course = courseService.createCourse(request);

        return GenericResponse.success(
                201,
                "Course created successfully",
                course
        );
    }

    // GET /api/v1/courses
    @GetMapping
    public GenericResponse<PageResponse<CourseResponse>> getCourses(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "title") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page number cannot be negative");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100");
        }

        if (departmentId != null && departmentId <= 0) {
            throw new IllegalArgumentException(
                    "Department ID must be positive");
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

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(direction, sortBy)
        );

        PageResponse<CourseResponse> courses =
                courseService.searchCourses(departmentId, q, pageable);

        return GenericResponse.success(
                200,
                "Courses retrieved successfully",
                courses
        );
    }

    // GET /api/v1/courses/{id}
    @GetMapping("/{id}")
    public GenericResponse<CourseResponse> getCourseById(
            @PathVariable Long id) {

        CourseResponse course = courseService.getCourseById(id);

        return GenericResponse.success(
                200,
                "Course retrieved successfully",
                course
        );
    }

    // PUT /api/v1/courses/{id}
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CATALOGUE_MANAGE')")
    public GenericResponse<CourseResponse> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseUpdateRequest request) {

        CourseResponse course = courseService.updateCourse(id, request);

        return GenericResponse.success(
                200,
                "Course updated successfully",
                course
        );
    }

    // PATCH /api/v1/courses/{id}/deactivate
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('CATALOGUE_MANAGE')")
    public GenericResponse<CourseResponse> deactivateCourse(
            @PathVariable Long id) {

        CourseResponse course = courseService.deactivateCourse(id);

        return GenericResponse.success(
                200,
                "Course deactivated successfully",
                course
        );
    }
}