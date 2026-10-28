
package rw.university.sims.controller;

import rw.university.sims.entity.CourseOffering;
import rw.university.sims.service.CourseOfferingService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CourseOfferingController.class)
class CourseOfferingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CourseOfferingService courseOfferingService;

    @Test
    void shouldCreateCourseOffering() throws Exception {
        CourseOffering course = new CourseOffering(
                1L, "CS101", "Introduction to Computer Science", 50
        );

        when(courseOfferingService.createCourseOffering(any(CourseOffering.class)))
                .thenReturn(course);

        mockMvc.perform(post("/api/course-offerings")
                        .with(user("testuser"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(course)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.courseCode").value("CS101"));
    }

    @Test
    void shouldReturnAllCourseOfferings() throws Exception {
        CourseOffering course = new CourseOffering(
                1L, "CS101", "Introduction to Computer Science", 50
        );

        when(courseOfferingService.getAllCourseOfferings())
                .thenReturn(List.of(course));

        mockMvc.perform(get("/api/course-offerings")
                        .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].courseCode").value("CS101"));
    }

    @Test
    void shouldReturnCourseOfferingById() throws Exception {
        CourseOffering course = new CourseOffering(
                1L, "CS101", "Introduction to Computer Science", 50
        );

        when(courseOfferingService.getCourseOfferingById(1L))
                .thenReturn(course);

        mockMvc.perform(get("/api/course-offerings/1")
                        .with(user("testuser")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseCode").value("CS101"));
    }
}
