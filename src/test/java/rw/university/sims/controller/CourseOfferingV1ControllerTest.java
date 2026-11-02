package rw.university.sims.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import rw.university.sims.dto.request.OfferingCreateRequest;
import rw.university.sims.dto.request.OfferingStatusRequest;
import rw.university.sims.dto.request.OfferingUpdateRequest;
import rw.university.sims.dto.response.CourseOfferingResponse;
import rw.university.sims.dto.response.OfferingRegistrationResponse;
import rw.university.sims.dto.response.PageResponse;
import rw.university.sims.enums.OfferingStatus;
import rw.university.sims.enums.RegistrationStatus;
import rw.university.sims.service.CourseOfferingManagementService;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CourseOfferingV1ControllerTest {

    @Mock
    private CourseOfferingManagementService offeringService;

    private MockMvc mockMvc;

    private CourseOfferingResponse offeringResponse() {
        return new CourseOfferingResponse(
                100L,
                1L,
                "CS101",
                "Introduction to Computing",
                2L,
                "Semester One",
                "2026-2027",
                "A",
                8L,
                "Lecturer One",
                (short) 3,
                30,
                5,
                25,
                OfferingStatus.OPEN,
                0L
        );
    }

    private void authenticate() {
        Instant now = Instant.now();

        Jwt jwt = Jwt.withTokenValue("test-token")
                .header("alg", "HS256")
                .subject("9")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(300))
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new JwtAuthenticationToken(jwt)
        );
    }

    @BeforeEach
    void setUp() {
        CourseOfferingV1Controller controller =
                new CourseOfferingV1Controller(offeringService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setCustomArgumentResolvers(
                        new AuthenticationPrincipalArgumentResolver()
                )
                .build();

        authenticate();
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCreateOffering() throws Exception {
        when(offeringService.createOffering(
                any(OfferingCreateRequest.class),
                eq(9L),
                anyString()
        )).thenReturn(offeringResponse());

        mockMvc.perform(post("/api/v1/course-offerings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "courseId": 1,
                                    "semesterId": 2,
                                    "section": "A",
                                    "lecturerId": 8,
                                    "capacity": 30
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.courseCode").value("CS101"))
                .andExpect(jsonPath("$.data.registeredCount").value(5));

        verify(offeringService).createOffering(
                any(OfferingCreateRequest.class),
                eq(9L),
                anyString()
        );
    }

    @Test
    void shouldListOfferings() throws Exception {
        PageResponse<CourseOfferingResponse> page = new PageResponse<>(
                List.of(offeringResponse()), 0, 20, 1, 1
        );

        when(offeringService.searchOfferings(
                isNull(), isNull(), isNull(), isNull(),
                any(PageRequest.class), eq(9L)
        )).thenReturn(page);

        mockMvc.perform(get("/api/v1/course-offerings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].id").value(100));

        verify(offeringService).searchOfferings(
                isNull(), isNull(), isNull(), isNull(),
                any(PageRequest.class), eq(9L)
        );
    }

    @Test
    void shouldGetOneOffering() throws Exception {
        when(offeringService.getOffering(100L, 9L))
                .thenReturn(offeringResponse());

        mockMvc.perform(get("/api/v1/course-offerings/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(100))
                .andExpect(jsonPath("$.data.seatsLeft").value(25));

        verify(offeringService).getOffering(100L, 9L);
    }

    @Test
    void shouldGetMyOfferings() throws Exception {
        PageResponse<CourseOfferingResponse> page = new PageResponse<>(
                List.of(offeringResponse()), 0, 20, 1, 1
        );

        when(offeringService.getMyOfferings(
                eq(9L), any(PageRequest.class)
        )).thenReturn(page);

        mockMvc.perform(get("/api/v1/course-offerings/mine"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1));

        verify(offeringService).getMyOfferings(
                eq(9L), any(PageRequest.class)
        );
    }

    @Test
    void shouldUpdateOffering() throws Exception {
        when(offeringService.updateOffering(
                eq(100L),
                any(OfferingUpdateRequest.class),
                eq(9L),
                anyString()
        )).thenReturn(offeringResponse());

        mockMvc.perform(put("/api/v1/course-offerings/100")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "capacity": 30,
                                    "lecturerId": 8,
                                    "version": 0
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(100));

        verify(offeringService).updateOffering(
                eq(100L),
                any(OfferingUpdateRequest.class),
                eq(9L),
                anyString()
        );
    }

    @Test
    void shouldChangeOfferingStatus() throws Exception {
        when(offeringService.changeOfferingStatus(
                eq(100L),
                any(OfferingStatusRequest.class),
                eq(9L),
                anyString()
        )).thenReturn(offeringResponse());

        mockMvc.perform(patch("/api/v1/course-offerings/100/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "status": "CLOSED"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200));

        verify(offeringService).changeOfferingStatus(
                eq(100L),
                any(OfferingStatusRequest.class),
                eq(9L),
                anyString()
        );
    }

    @Test
    void shouldGetOfferingRegistrations() throws Exception {
        OfferingRegistrationResponse registration =
                new OfferingRegistrationResponse(
                        200L,
                        1L,
                        "UGL001",
                        "Student",
                        "One",
                        "student@example.com",
                        RegistrationStatus.REGISTERED,
                        LocalDateTime.of(2026, 10, 1, 10, 0),
                        null
                );

        PageResponse<OfferingRegistrationResponse> page =
                new PageResponse<>(List.of(registration), 0, 20, 1, 1);

        when(offeringService.getOfferingRegistrations(
                eq(100L), eq(9L), any(PageRequest.class)
        )).thenReturn(page);

        mockMvc.perform(get(
                        "/api/v1/course-offerings/100/registrations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content.length()").value(1))
                .andExpect(jsonPath("$.data.content[0].studentNumber")
                        .value("UGL001"));

        verify(offeringService).getOfferingRegistrations(
                eq(100L), eq(9L), any(PageRequest.class)
        );
    }
}