package rw.university.sims.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import rw.university.sims.dto.request.CourseRequest;
import rw.university.sims.dto.request.CourseUpdateRequest;
import rw.university.sims.dto.response.CourseResponse;
import rw.university.sims.dto.response.PageResponse;
import rw.university.sims.exception.GlobalExceptionHandler;
import rw.university.sims.exception.ResourceConflictException;
import rw.university.sims.service.CourseService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CourseControllerTest {

    @Mock
    private CourseService courseService;

    private MockMvc mockMvc;

    private CourseResponse createCourseResponse() {
        return new CourseResponse(
                1L,
                "CS101",
                "Introduction to Computing",
                3,
                10L,
                "CS",
                "Computer Science",
                true
        );
    }

    @BeforeEach
    void setUp() {
        CourseController controller = new CourseController(courseService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldCreateCourse() throws Exception {

        CourseResponse course = createCourseResponse();

        when(courseService.createCourse(any(CourseRequest.class)))
                .thenReturn(course);

        mockMvc.perform(post("/api/v1/courses")
                        .contentType("application/json")
                        .content("""
                                {
                                    "courseCode": "CS101",
                                    "title": "Introduction to Computing",
                                    "credits": 3,
                                    "departmentId": 10
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.courseCode").value("CS101"))
                .andExpect(jsonPath("$.data.title")
                        .value("Introduction to Computing"))
                .andExpect(jsonPath("$.data.credits").value(3))
                .andExpect(jsonPath("$.data.departmentId").value(10))
                .andExpect(jsonPath("$.data.departmentCode").value("CS"))
                .andExpect(jsonPath("$.data.active").value(true));

        ArgumentCaptor<CourseRequest> captor =
                ArgumentCaptor.forClass(CourseRequest.class);

        verify(courseService).createCourse(captor.capture());

        assertEquals("CS101", captor.getValue().courseCode());
        assertEquals(3, captor.getValue().credits());
        assertEquals(10L, captor.getValue().departmentId());
    }

    @Test
    void shouldGetCourses() throws Exception {

        CourseResponse course = createCourseResponse();

        PageResponse<CourseResponse> pageResponse =
                new PageResponse<>(
                        List.of(course),
                        0,
                        20,
                        1,
                        1
                );

        when(courseService.searchCourses(
                eq((Long) null),
                eq((String) null),
                any(Pageable.class)
        )).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(1))
                .andExpect(jsonPath("$.data.content[0].courseCode")
                        .value("CS101"))
                .andExpect(jsonPath("$.data.page").value(0))
                .andExpect(jsonPath("$.data.size").value(20))
                .andExpect(jsonPath("$.data.totalElements").value(1))
                .andExpect(jsonPath("$.data.totalPages").value(1));

        verify(courseService).searchCourses(
                eq((Long) null),
                eq((String) null),
                any(Pageable.class)
        );
    }

    @Test
    void shouldFilterCoursesByDepartmentAndSearchText()
            throws Exception {

        CourseResponse course = createCourseResponse();

        PageResponse<CourseResponse> pageResponse =
                new PageResponse<>(
                        List.of(course),
                        1,
                        5,
                        6,
                        2
                );

        when(courseService.searchCourses(
                eq(10L),
                eq("computing"),
                any(Pageable.class)
        )).thenReturn(pageResponse);

        mockMvc.perform(get("/api/v1/courses")
                        .param("departmentId", "10")
                        .param("q", "computing")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sortBy", "courseCode")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.page").value(1))
                .andExpect(jsonPath("$.data.size").value(5))
                .andExpect(jsonPath("$.data.totalElements").value(6))
                .andExpect(jsonPath("$.data.totalPages").value(2));

        ArgumentCaptor<Pageable> pageableCaptor =
                ArgumentCaptor.forClass(Pageable.class);

        verify(courseService).searchCourses(
                eq(10L),
                eq("computing"),
                pageableCaptor.capture()
        );

        Pageable pageable = pageableCaptor.getValue();

        assertEquals(1, pageable.getPageNumber());
        assertEquals(5, pageable.getPageSize());
        assertEquals(
                "courseCode",
                pageable.getSort().getOrderFor("courseCode").getProperty()
        );
        assertEquals(
                "DESC",
                pageable.getSort().getOrderFor("courseCode")
                        .getDirection().name()
        );
    }

    @Test
    void shouldGetCourseById() throws Exception {

        CourseResponse course = createCourseResponse();

        when(courseService.getCourseById(1L))
                .thenReturn(course);

        mockMvc.perform(get("/api/v1/courses/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.courseCode").value("CS101"))
                .andExpect(jsonPath("$.data.title")
                        .value("Introduction to Computing"))
                .andExpect(jsonPath("$.data.departmentName")
                        .value("Computer Science"));

        verify(courseService).getCourseById(1L);
    }

    @Test
    void shouldRejectInvalidPageSize() throws Exception {

        mockMvc.perform(get("/api/v1/courses")
                        .param("size", "101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(courseService);
    }

    @Test
    void shouldUpdateCourse() throws Exception {

        CourseResponse course = new CourseResponse(
                1L,
                "CS101",
                "Advanced Computing",
                4,
                10L,
                "CS",
                "Computer Science",
                true
        );

        when(courseService.updateCourse(
                eq(1L),
                any(CourseUpdateRequest.class)
        )).thenReturn(course);

        mockMvc.perform(put("/api/v1/courses/1")
                        .contentType("application/json")
                        .content("""
                                {
                                    "title": "Advanced Computing",
                                    "credits": 4,
                                    "departmentId": 10
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.courseCode").value("CS101"))
                .andExpect(jsonPath("$.data.title")
                        .value("Advanced Computing"))
                .andExpect(jsonPath("$.data.credits").value(4));

        ArgumentCaptor<CourseUpdateRequest> captor =
                ArgumentCaptor.forClass(CourseUpdateRequest.class);

        verify(courseService).updateCourse(eq(1L), captor.capture());

        assertEquals("Advanced Computing", captor.getValue().title());
        assertEquals(4, captor.getValue().credits());
        assertEquals(10L, captor.getValue().departmentId());
    }

    @Test
    void shouldDeactivateCourse() throws Exception {

        CourseResponse inactiveCourse = new CourseResponse(
                1L,
                "CS101",
                "Introduction to Computing",
                3,
                10L,
                "CS",
                "Computer Science",
                false
        );

        when(courseService.deactivateCourse(1L))
                .thenReturn(inactiveCourse);

        mockMvc.perform(patch("/api/v1/courses/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.courseCode").value("CS101"))
                .andExpect(jsonPath("$.data.active").value(false));

        verify(courseService).deactivateCourse(1L);
    }

    @Test
    void shouldReturnConflictWhenCourseCannotBeDeactivated()
            throws Exception {

        when(courseService.deactivateCourse(1L))
                .thenThrow(new ResourceConflictException(
                        "Course is already inactive"
                ));

        mockMvc.perform(patch("/api/v1/courses/1/deactivate"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("RESOURCE_CONFLICT"))
                .andExpect(jsonPath("$.message")
                        .value("Course is already inactive"));

        verify(courseService).deactivateCourse(1L);
    }

    @Test
    void shouldReturnConflictWhenCourseUpdateHasConflict()
            throws Exception {

        when(courseService.updateCourse(
                eq(1L),
                any(CourseUpdateRequest.class)
        )).thenThrow(new ResourceConflictException(
                "Cannot assign a course to an inactive department"
        ));

        mockMvc.perform(put("/api/v1/courses/1")
                        .contentType("application/json")
                        .content("""
                                {
                                    "title": "Advanced Computing",
                                    "credits": 4,
                                    "departmentId": 10
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("RESOURCE_CONFLICT"));

        verify(courseService).updateCourse(
                eq(1L),
                any(CourseUpdateRequest.class)
        );
    }
}