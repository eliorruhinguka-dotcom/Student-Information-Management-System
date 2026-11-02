package rw.university.sims.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rw.university.sims.dto.request.CoursePrerequisiteRequest;
import rw.university.sims.dto.response.CourseResponse;
import rw.university.sims.entity.Course;
import rw.university.sims.entity.CoursePrerequisite;
import rw.university.sims.entity.CoursePrerequisiteId;
import rw.university.sims.entity.Department;
import rw.university.sims.entity.UserAccount;
import rw.university.sims.exception.ResourceConflictException;
import rw.university.sims.exception.ResourceNotFoundException;
import rw.university.sims.repository.CoursePrerequisiteRepository;
import rw.university.sims.repository.CourseRepository;
import rw.university.sims.repository.UserAccountRepository;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Deque;

@Service
@Transactional
public class CoursePrerequisiteService {

    private final CourseRepository courseRepository;
    private final CoursePrerequisiteRepository prerequisiteRepository;
    private final UserAccountRepository userAccountRepository;
    private final AuditService auditService;

    public CoursePrerequisiteService(
            CourseRepository courseRepository,
            CoursePrerequisiteRepository prerequisiteRepository,
            UserAccountRepository userAccountRepository,
            AuditService auditService) {

        this.courseRepository = courseRepository;
        this.prerequisiteRepository = prerequisiteRepository;
        this.userAccountRepository = userAccountRepository;
        this.auditService = auditService;
    }

    // GET: List all prerequisites for a course.
    @Transactional(readOnly = true)
    public List<CourseResponse> getPrerequisites(Long courseId) {

        findCourse(courseId);

        return prerequisiteRepository.findAllForCourse(courseId)
                .stream()
                .map(link -> toCourseResponse(link.getPrerequisiteCourse()))
                .toList();
    }

    // POST: Add a prerequisite to a course.
    public CourseResponse addPrerequisite(
            Long courseId,
            CoursePrerequisiteRequest request) {

        Course course = findCourse(courseId);
        Long prerequisiteId = request.prerequisiteCourseId();

        Course prerequisiteCourse = findCourse(prerequisiteId);

        if (courseId.equals(prerequisiteId)) {
            throw new ResourceConflictException(
                    "A course cannot be its own prerequisite");
        }

        if (prerequisiteRepository
                .existsByIdCourseIdAndIdPrerequisiteCourseId(
                        courseId, prerequisiteId)) {
            throw new ResourceConflictException(
                    "This prerequisite is already assigned to the course");
        }

        if (wouldCreateCycle(courseId, prerequisiteId)) {
            throw new ResourceConflictException(
                    "This prerequisite would create a circular course dependency");
        }

        CoursePrerequisite link =
                new CoursePrerequisite(course, prerequisiteCourse);

        prerequisiteRepository.save(link);

        return toCourseResponse(prerequisiteCourse);
    }

    // DELETE: Remove a prerequisite and record the action in the audit log.
    public void removePrerequisite(
            Long courseId,
            Long prerequisiteId,
            Long actorUserId,
            String ipAddress) {

        findCourse(courseId);
        findCourse(prerequisiteId);

        CoursePrerequisiteId linkId =
                new CoursePrerequisiteId(courseId, prerequisiteId);

        CoursePrerequisite link = prerequisiteRepository.findById(linkId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prerequisite relationship was not found"));

        UserAccount actor = userAccountRepository.findById(actorUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Authenticated user account was not found"));

        prerequisiteRepository.delete(link);

        auditService.recordWithValues(
                actor,
                "COURSE_PREREQUISITE_REMOVED",
                "COURSE_PREREQUISITE",
                courseId,
                "SUCCESS",
                ipAddress,
                null,
                Map.of(
                        "courseId", courseId,
                        "prerequisiteCourseId", prerequisiteId
                ),
                null
        );
    }

    // A -> B means course A requires course B.
    // Adding A -> B is invalid if B already depends on A, directly or indirectly.
    private boolean wouldCreateCycle(
            Long courseId,
            Long prerequisiteId) {

        Deque<Long> coursesToVisit = new ArrayDeque<>();
        Set<Long> visited = new HashSet<>();

        coursesToVisit.push(prerequisiteId);

        while (!coursesToVisit.isEmpty()) {
            Long currentCourseId = coursesToVisit.pop();

            if (currentCourseId.equals(courseId)) {
                return true;
            }

            if (visited.add(currentCourseId)) {
                List<Long> existingPrerequisites =
                        prerequisiteRepository.findPrerequisiteIdsByCourseId(
                                currentCourseId
                        );

                for (Long existingId : existingPrerequisites) {
                    if (!visited.contains(existingId)) {
                        coursesToVisit.push(existingId);
                    }
                }
            }
        }

        return false;
    }

    private Course findCourse(Long id) {
        return courseRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found with ID: " + id));
    }

    private CourseResponse toCourseResponse(Course course) {
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