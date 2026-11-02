package rw.university.sims.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import rw.university.sims.dto.request.CoursePrerequisiteRequest;
import rw.university.sims.dto.response.CourseResponse;
import rw.university.sims.exception.GlobalExceptionHandler;
import rw.university.sims.service.CoursePrerequisiteService;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CoursePrerequisiteControllerTest {

    @Mock
    private CoursePrerequisiteService prerequisiteService;

    private MockMvc mockMvc;

    private CourseResponse createCourseResponse() {
        return new CourseResponse(
                2L,
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
        CoursePrerequisiteController controller =
                new CoursePrerequisiteController(prerequisiteService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(
                        new AuthenticationPrincipalArgumentResolver()
                )
                .build();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldGetPrerequisites() throws Exception {
        when(prerequisiteService.getPrerequisites(1L))
                .thenReturn(List.of(createCourseResponse()));

        mockMvc.perform(get("/api/v1/courses/1/prerequisites"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(2))
                .andExpect(jsonPath("$.data[0].courseCode").value("CS101"));

        verify(prerequisiteService).getPrerequisites(1L);
    }

    @Test
    void shouldAddPrerequisite() throws Exception {
        when(prerequisiteService.addPrerequisite(
                eq(1L),
                any(CoursePrerequisiteRequest.class)
        )).thenReturn(createCourseResponse());

        mockMvc.perform(post("/api/v1/courses/1/prerequisites")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "prerequisiteCourseId": 2
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(2))
                .andExpect(jsonPath("$.data.courseCode").value("CS101"));

        verify(prerequisiteService).addPrerequisite(
                eq(1L),
                any(CoursePrerequisiteRequest.class)
        );
    }

    @Test
    void shouldRemovePrerequisiteUsingAuthenticatedUser()
            throws Exception {

        Instant now = Instant.now();

        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject("7")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(jwt)
        );

        mockMvc.perform(delete(
                        "/api/v1/courses/1/prerequisites/2"))
                .andDo(org.springframework.test.web.servlet.result.MockMvcResultHandlers.print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.message")
                        .value("Course prerequisite removed successfully"));

        verify(prerequisiteService).removePrerequisite(
                1L,
                2L,
                7L,
                "127.0.0.1"
        );
    }
}