package rw.university.sims.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private CourseService courseService;

    private Department createDepartment() {
        Department department = mock(Department.class);

        when(department.getId()).thenReturn(10L);
        when(department.getCode()).thenReturn("CS");
        when(department.getName()).thenReturn("Computer Science");

        return department;
    }

    private Course createCourse(Department department) {
        Course course = mock(Course.class);

        when(course.getId()).thenReturn(1L);
        when(course.getCourseCode()).thenReturn("CS101");
        when(course.getTitle()).thenReturn("Introduction to Computing");
        when(course.getCredits()).thenReturn(3);
        when(course.getDepartment()).thenReturn(department);
        when(course.isActive()).thenReturn(true);

        return course;
    }

    @Test
    void shouldCreateCourse() {
        Department department = createDepartment();
        when(department.isActive()).thenReturn(true);

        Course savedCourse = createCourse(department);

        CourseRequest request = new CourseRequest(
                " cs101 ",
                " Introduction to Computing ",
                3,
                10L
        );

        when(courseRepository.existsByCourseCodeIgnoreCase("CS101"))
                .thenReturn(false);

        when(departmentRepository.findById(10L))
                .thenReturn(Optional.of(department));

        when(courseRepository.save(any(Course.class)))
                .thenReturn(savedCourse);

        CourseResponse response = courseService.createCourse(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("CS101", response.courseCode());
        assertEquals("Introduction to Computing", response.title());
        assertEquals(3, response.credits());
        assertEquals(10L, response.departmentId());
        assertEquals("CS", response.departmentCode());
        assertTrue(response.active());

        ArgumentCaptor<Course> captor =
                ArgumentCaptor.forClass(Course.class);

        verify(courseRepository).save(captor.capture());

        assertEquals("CS101", captor.getValue().getCourseCode());
        assertEquals(
                "Introduction to Computing",
                captor.getValue().getTitle()
        );
        assertEquals(3, captor.getValue().getCredits());
        assertSame(department, captor.getValue().getDepartment());
    }

    @Test
    void shouldRejectDuplicateCourseCode() {
        CourseRequest request = new CourseRequest(
                "CS101",
                "Introduction to Computing",
                3,
                10L
        );

        when(courseRepository.existsByCourseCodeIgnoreCase("CS101"))
                .thenReturn(true);

        assertThrows(
                ResourceConflictException.class,
                () -> courseService.createCourse(request)
        );

        verify(departmentRepository, never()).findById(anyLong());
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void shouldRejectCourseWhenDepartmentDoesNotExist() {
        CourseRequest request = new CourseRequest(
                "CS101",
                "Introduction to Computing",
                3,
                99L
        );

        when(courseRepository.existsByCourseCodeIgnoreCase("CS101"))
                .thenReturn(false);

        when(departmentRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> courseService.createCourse(request)
        );

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void shouldRejectCourseWhenDepartmentIsInactive() {
        Department department = mock(Department.class);
        when(department.isActive()).thenReturn(false);

        CourseRequest request = new CourseRequest(
                "CS101",
                "Introduction to Computing",
                3,
                10L
        );

        when(courseRepository.existsByCourseCodeIgnoreCase("CS101"))
                .thenReturn(false);

        when(departmentRepository.findById(10L))
                .thenReturn(Optional.of(department));

        assertThrows(
                ResourceConflictException.class,
                () -> courseService.createCourse(request)
        );

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void shouldSearchCoursesWithPagination() {
        Department department = createDepartment();
        Course course = createCourse(department);

        PageRequest pageable = PageRequest.of(0, 20);

        Page<Course> coursePage = new PageImpl<>(
                List.of(course),
                pageable,
                1
        );

        when(courseRepository.searchCourses(
                10L,
                "computing",
                pageable
        )).thenReturn(coursePage);

        PageResponse<CourseResponse> response =
                courseService.searchCourses(
                        10L,
                        " computing ",
                        pageable
                );

        assertEquals(1, response.content().size());
        assertEquals(0, response.page());
        assertEquals(20, response.size());
        assertEquals(1, response.totalElements());
        assertEquals(1, response.totalPages());

        assertEquals(
                "Introduction to Computing",
                response.content().get(0).title()
        );

        verify(courseRepository).searchCourses(
                10L,
                "computing",
                pageable
        );
    }

    @Test
    void shouldGetCourseById() {
        Department department = createDepartment();
        Course course = createCourse(department);

        when(courseRepository.findDetailedById(1L))
                .thenReturn(Optional.of(course));

        CourseResponse response = courseService.getCourseById(1L);

        assertEquals(1L, response.id());
        assertEquals("CS101", response.courseCode());
        assertEquals("Introduction to Computing", response.title());
        assertEquals(3, response.credits());
        assertEquals("Computer Science", response.departmentName());
    }

    @Test
    void shouldThrowWhenCourseDoesNotExist() {
        when(courseRepository.findDetailedById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> courseService.getCourseById(99L)
        );
    }

    @Test
    void shouldUpdateCourse() {
        Department newDepartment = createDepartment();
        when(newDepartment.isActive()).thenReturn(true);

        Department oldDepartment =
                new Department("OLD", "Old Department");

        Course existingCourse = new Course(
                "CS101",
                "Old Course Title",
                2,
                oldDepartment
        );

        CourseUpdateRequest request = new CourseUpdateRequest(
                " Advanced Computing ",
                4,
                10L
        );

        when(courseRepository.findById(1L))
                .thenReturn(Optional.of(existingCourse));

        when(departmentRepository.findById(10L))
                .thenReturn(Optional.of(newDepartment));

        when(courseRepository.save(any(Course.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CourseResponse response =
                courseService.updateCourse(1L, request);

        assertEquals("CS101", existingCourse.getCourseCode());
        assertEquals("Advanced Computing", existingCourse.getTitle());
        assertEquals(4, existingCourse.getCredits());
        assertSame(newDepartment, existingCourse.getDepartment());

        assertEquals("Advanced Computing", response.title());
        assertEquals(4, response.credits());
        assertEquals(10L, response.departmentId());

        verify(courseRepository).save(existingCourse);
    }

    @Test
    void shouldRejectUpdateWhenCourseDoesNotExist() {
        CourseUpdateRequest request = new CourseUpdateRequest(
                "Advanced Computing",
                4,
                10L
        );

        when(courseRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> courseService.updateCourse(99L, request)
        );

        verify(departmentRepository, never()).findById(anyLong());
        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void shouldRejectUpdateWhenDepartmentDoesNotExist() {
        Department oldDepartment =
                new Department("OLD", "Old Department");

        Course existingCourse = new Course(
                "CS101",
                "Introduction to Computing",
                3,
                oldDepartment
        );

        CourseUpdateRequest request = new CourseUpdateRequest(
                "Advanced Computing",
                4,
                99L
        );

        when(courseRepository.findById(1L))
                .thenReturn(Optional.of(existingCourse));

        when(departmentRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> courseService.updateCourse(1L, request)
        );

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void shouldRejectUpdateWhenDepartmentIsInactive() {
        Department inactiveDepartment = mock(Department.class);
        when(inactiveDepartment.isActive()).thenReturn(false);

        Department oldDepartment =
                new Department("OLD", "Old Department");

        Course existingCourse = new Course(
                "CS101",
                "Introduction to Computing",
                3,
                oldDepartment
        );

        CourseUpdateRequest request = new CourseUpdateRequest(
                "Advanced Computing",
                4,
                10L
        );

        when(courseRepository.findById(1L))
                .thenReturn(Optional.of(existingCourse));

        when(departmentRepository.findById(10L))
                .thenReturn(Optional.of(inactiveDepartment));

        assertThrows(
                ResourceConflictException.class,
                () -> courseService.updateCourse(1L, request)
        );

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void shouldDeactivateCourse() {
        Department department = createDepartment();

        Course course = new Course(
                "CS101",
                "Introduction to Computing",
                3,
                department
        );

        when(courseRepository.findDetailedById(1L))
                .thenReturn(Optional.of(course));

        when(courseRepository.save(any(Course.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CourseResponse response =
                courseService.deactivateCourse(1L);

        assertFalse(course.isActive());
        assertFalse(response.active());
        assertEquals("CS101", response.courseCode());

        verify(courseRepository).save(course);
    }

    @Test
    void shouldRejectDeactivationWhenCourseDoesNotExist() {
        when(courseRepository.findDetailedById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> courseService.deactivateCourse(99L)
        );

        verify(courseRepository, never()).save(any(Course.class));
    }

    @Test
    void shouldRejectDeactivationWhenCourseIsAlreadyInactive() {
        Department department =
                new Department("CS", "Computer Science");

        Course course = new Course(
                "CS101",
                "Introduction to Computing",
                3,
                department
        );

        course.setActive(false);

        when(courseRepository.findDetailedById(1L))
                .thenReturn(Optional.of(course));

        assertThrows(
                ResourceConflictException.class,
                () -> courseService.deactivateCourse(1L)
        );

        verify(courseRepository, never()).save(any(Course.class));
    }
}