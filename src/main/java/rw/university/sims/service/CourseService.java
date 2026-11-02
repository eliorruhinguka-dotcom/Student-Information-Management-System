package rw.university.sims.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rw.university.sims.dto.request.CourseRequest;
import rw.university.sims.dto.request.CourseUpdateRequest;
import rw.university.sims.dto.response.CourseResponse;
import rw.university.sims.dto.response.PageResponse;
import rw.university.sims.entity.Course;
import rw.university.sims.entity.Department;
import rw.university.sims.exception.ResourceConflictException;
import rw.university.sims.exception.ResourceNotFoundException;
import rw.university.sims.repository.CourseRepository;
import rw.university.sims.repository.DepartmentRepository;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentRepository departmentRepository;

    public CourseService(
            CourseRepository courseRepository,
            DepartmentRepository departmentRepository) {

        this.courseRepository = courseRepository;
        this.departmentRepository = departmentRepository;
    }

    // Create a course
    public CourseResponse createCourse(CourseRequest request) {

        String courseCode = request.courseCode()
                .trim()
                .toUpperCase(java.util.Locale.ROOT);

        if (courseRepository.existsByCourseCodeIgnoreCase(courseCode)) {
            throw new ResourceConflictException(
                    "Course code already exists");
        }

        Department department = departmentRepository
                .findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with ID: "
                                + request.departmentId()));

        if (!department.isActive()) {
            throw new ResourceConflictException(
                    "Cannot create a course under an inactive department");
        }

        Course course = new Course(
                courseCode,
                request.title().trim(),
                request.credits(),
                department
        );

        return toResponse(courseRepository.save(course));
    }

    // Search and list courses with pagination
    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> searchCourses(
            Long departmentId,
            String searchText,
            Pageable pageable) {

        String query = searchText == null || searchText.isBlank()
                ? null
                : searchText.trim();

        Page<Course> coursePage = courseRepository.searchCourses(
                departmentId,
                query,
                pageable
        );

        return new PageResponse<>(
                coursePage.getContent()
                        .stream()
                        .map(this::toResponse)
                        .toList(),
                coursePage.getNumber(),
                coursePage.getSize(),
                coursePage.getTotalElements(),
                coursePage.getTotalPages()
        );
    }

    // Get one course
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long id) {

        Course course = courseRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found with ID: " + id));

        return toResponse(course);
    }

    // Update course title, credits and department.
    // Course code is intentionally not editable.
    public CourseResponse updateCourse(
            Long id,
            CourseUpdateRequest request) {

        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found with ID: " + id));

        Department department = departmentRepository
                .findById(request.departmentId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with ID: "
                                + request.departmentId()));

        if (!department.isActive()) {
            throw new ResourceConflictException(
                    "Cannot assign a course to an inactive department");
        }

        course.setTitle(request.title().trim());
        course.setCredits(request.credits());
        course.setDepartment(department);

        return toResponse(courseRepository.save(course));
    }

    // Deactivate a course instead of deleting it.
    public CourseResponse deactivateCourse(Long id) {

        Course course = courseRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found with ID: " + id));

        if (!course.isActive()) {
            throw new ResourceConflictException(
                    "Course is already inactive");
        }

        course.setActive(false);

        return toResponse(courseRepository.save(course));
    }

    private CourseResponse toResponse(Course course) {

        Department department = course.getDepartment();

        return new CourseResponse(
                course.getId(),
                course.getCourseCode(),
                course.getTitle(),
                course.getCredits(),
                department.getId(),
                department.getCode(),
                department.getName(),
                course.isActive()
        );
    }
}