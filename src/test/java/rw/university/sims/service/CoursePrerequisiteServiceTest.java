package rw.university.sims.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.LenientStubber;

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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoursePrerequisiteServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private CoursePrerequisiteRepository prerequisiteRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private CoursePrerequisiteService prerequisiteService;

    private Course createCourse(Long id, String code, String title) {
        Department department = mock(Department.class);

        lenient().when(department.getId()).thenReturn(10L);
        lenient().when(department.getCode()).thenReturn("CS");
        lenient().when(department.getName()).thenReturn("Computer Science");

        Course course = mock(Course.class);

        lenient().when(course.getId()).thenReturn(id);
        lenient().when(course.getCourseCode()).thenReturn(code);
        lenient().when(course.getTitle()).thenReturn(title);
        lenient().when(course.getCredits()).thenReturn(3);
        lenient().when(course.getDepartment()).thenReturn(department);
        lenient().when(course.isActive()).thenReturn(true);

        return course;
    }

    @Test
    void shouldGetCoursePrerequisites() {
        Course parentCourse =
                createCourse(1L, "CS201", "Advanced Computing");
        Course prerequisite =
                createCourse(2L, "CS101", "Introduction to Computing");

        CoursePrerequisite link = mock(CoursePrerequisite.class);
        when(link.getPrerequisiteCourse()).thenReturn(prerequisite);

        when(courseRepository.findDetailedById(1L))
                .thenReturn(Optional.of(parentCourse));
        when(prerequisiteRepository.findAllForCourse(1L))
                .thenReturn(List.of(link));

        List<CourseResponse> response =
                prerequisiteService.getPrerequisites(1L);

        assertEquals(1, response.size());
        assertEquals(2L, response.get(0).id());
        assertEquals("CS101", response.get(0).courseCode());

        verify(prerequisiteRepository).findAllForCourse(1L);
    }

    @Test
    void shouldRejectGettingPrerequisitesForMissingCourse() {
        when(courseRepository.findDetailedById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> prerequisiteService.getPrerequisites(99L)
        );

        verifyNoInteractions(prerequisiteRepository);
    }

    @Test
    void shouldAddPrerequisite() {
        Course parentCourse =
                createCourse(1L, "CS201", "Advanced Computing");
        Course prerequisite =
                createCourse(2L, "CS101", "Introduction to Computing");

        when(courseRepository.findDetailedById(1L))
                .thenReturn(Optional.of(parentCourse));
        when(courseRepository.findDetailedById(2L))
                .thenReturn(Optional.of(prerequisite));
        when(prerequisiteRepository
                .existsByIdCourseIdAndIdPrerequisiteCourseId(1L, 2L))
                .thenReturn(false);
        when(prerequisiteRepository.findPrerequisiteIdsByCourseId(2L))
                .thenReturn(List.of());

        CourseResponse response = prerequisiteService.addPrerequisite(
                1L,
                new CoursePrerequisiteRequest(2L)
        );

        assertEquals(2L, response.id());
        assertEquals("CS101", response.courseCode());

        verify(prerequisiteRepository).save(any(CoursePrerequisite.class));
    }

    @Test
    void shouldRejectDuplicatePrerequisite() {
        Course parentCourse =
                createCourse(1L, "CS201", "Advanced Computing");
        Course prerequisite =
                createCourse(2L, "CS101", "Introduction to Computing");

        when(courseRepository.findDetailedById(1L))
                .thenReturn(Optional.of(parentCourse));
        when(courseRepository.findDetailedById(2L))
                .thenReturn(Optional.of(prerequisite));
        when(prerequisiteRepository
                .existsByIdCourseIdAndIdPrerequisiteCourseId(1L, 2L))
                .thenReturn(true);

        assertThrows(
                ResourceConflictException.class,
                () -> prerequisiteService.addPrerequisite(
                        1L,
                        new CoursePrerequisiteRequest(2L)
                )
        );

        verify(prerequisiteRepository, never())
                .save(any(CoursePrerequisite.class));
    }

    @Test
    void shouldRejectCourseAsItsOwnPrerequisite() {
        Course course =
                createCourse(1L, "CS101", "Introduction to Computing");

        when(courseRepository.findDetailedById(1L))
                .thenReturn(Optional.of(course));

        assertThrows(
                ResourceConflictException.class,
                () -> prerequisiteService.addPrerequisite(
                        1L,
                        new CoursePrerequisiteRequest(1L)
                )
        );

        verify(prerequisiteRepository, never())
                .save(any(CoursePrerequisite.class));
    }

    @Test
    void shouldRejectCircularPrerequisite() {
        Course courseA = createCourse(1L, "CS101", "Course A");
        Course courseB = createCourse(2L, "CS201", "Course B");

        when(courseRepository.findDetailedById(1L))
                .thenReturn(Optional.of(courseA));
        when(courseRepository.findDetailedById(2L))
                .thenReturn(Optional.of(courseB));
        when(prerequisiteRepository
                .existsByIdCourseIdAndIdPrerequisiteCourseId(1L, 2L))
                .thenReturn(false);

        // B requires C, and C requires A.
        when(prerequisiteRepository.findPrerequisiteIdsByCourseId(2L))
                .thenReturn(List.of(3L));
        when(prerequisiteRepository.findPrerequisiteIdsByCourseId(3L))
                .thenReturn(List.of(1L));

        assertThrows(
                ResourceConflictException.class,
                () -> prerequisiteService.addPrerequisite(
                        1L,
                        new CoursePrerequisiteRequest(2L)
                )
        );

        verify(prerequisiteRepository, never())
                .save(any(CoursePrerequisite.class));
    }

    @Test
    void shouldRejectPrerequisiteWhenItDoesNotExist() {
        Course parentCourse =
                createCourse(1L, "CS201", "Advanced Computing");

        when(courseRepository.findDetailedById(1L))
                .thenReturn(Optional.of(parentCourse));
        when(courseRepository.findDetailedById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> prerequisiteService.addPrerequisite(
                        1L,
                        new CoursePrerequisiteRequest(99L)
                )
        );

        verify(prerequisiteRepository, never())
                .save(any(CoursePrerequisite.class));
    }

    @Test
    void shouldRemovePrerequisiteAndRecordAudit() {
        Course parentCourse =
                createCourse(1L, "CS201", "Advanced Computing");
        Course prerequisite =
                createCourse(2L, "CS101", "Introduction to Computing");

        CoursePrerequisite link = mock(CoursePrerequisite.class);
        UserAccount actor = mock(UserAccount.class);

        when(courseRepository.findDetailedById(1L))
                .thenReturn(Optional.of(parentCourse));
        when(courseRepository.findDetailedById(2L))
                .thenReturn(Optional.of(prerequisite));
        when(prerequisiteRepository.findById(
                new CoursePrerequisiteId(1L, 2L)))
                .thenReturn(Optional.of(link));
        when(userAccountRepository.findById(7L))
                .thenReturn(Optional.of(actor));

        prerequisiteService.removePrerequisite(
                1L, 2L, 7L, "127.0.0.1"
        );

        verify(prerequisiteRepository).delete(link);

        verify(auditService).recordWithValues(
                actor,
                "COURSE_PREREQUISITE_REMOVED",
                "COURSE_PREREQUISITE",
                1L,
                "SUCCESS",
                "127.0.0.1",
                null,
                Map.of(
                        "courseId", 1L,
                        "prerequisiteCourseId", 2L
                ),
                null
        );
    }

    @Test
    void shouldRejectRemovingMissingPrerequisiteRelationship() {
        Course parentCourse =
                createCourse(1L, "CS201", "Advanced Computing");
        Course prerequisite =
                createCourse(2L, "CS101", "Introduction to Computing");

        when(courseRepository.findDetailedById(1L))
                .thenReturn(Optional.of(parentCourse));
        when(courseRepository.findDetailedById(2L))
                .thenReturn(Optional.of(prerequisite));
        when(prerequisiteRepository.findById(
                new CoursePrerequisiteId(1L, 2L)))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> prerequisiteService.removePrerequisite(
                        1L, 2L, 7L, "127.0.0.1"
                )
        );

        verify(prerequisiteRepository, never())
                .delete(any(CoursePrerequisite.class));

        verifyNoInteractions(auditService);
    }
}