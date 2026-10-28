
package rw.university.sims.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import rw.university.sims.entity.CourseOffering;
import rw.university.sims.entity.CourseRegistration;
import rw.university.sims.entity.Student;
import rw.university.sims.enums.RegistrationStatus;
import rw.university.sims.service.CourseRegistrationService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationControllerTest {

    @Mock
    private CourseRegistrationService registrationService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CourseRegistrationController controller =
                new CourseRegistrationController(registrationService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void shouldCreateRegistration() throws Exception {
        CourseRegistration registration = createRegistration(
                RegistrationStatus.REGISTERED, null);

        when(registrationService.registerStudent(1L, 10L))
                .thenReturn(registration);

        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "studentId": 1,
                                    "courseOfferingId": 10
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.studentId").value(1))
                .andExpect(jsonPath("$.courseOfferingId").value(10))
                .andExpect(jsonPath("$.status").value("REGISTERED"))
                .andExpect(jsonPath("$.cancelledAt").doesNotExist());

        verify(registrationService).registerStudent(1L, 10L);
    }

    @Test
    void shouldGetAllRegistrations() throws Exception {
        CourseRegistration registration = createRegistration(
                RegistrationStatus.REGISTERED, null);

        when(registrationService.getAllRegistrations())
                .thenReturn(List.of(registration));

        mockMvc.perform(get("/api/registrations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].status").value("REGISTERED"));
    }

    @Test
    void shouldGetRegistrationById() throws Exception {
        CourseRegistration registration = createRegistration(
                RegistrationStatus.REGISTERED, null);

        when(registrationService.getRegistrationById(100L))
                .thenReturn(registration);

        mockMvc.perform(get("/api/registrations/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.studentId").value(1))
                .andExpect(jsonPath("$.courseOfferingId").value(10))
                .andExpect(jsonPath("$.status").value("REGISTERED"));
    }

    @Test
    void shouldCancelRegistration() throws Exception {
        LocalDateTime cancelledAt =
                LocalDateTime.of(2026, 10, 28, 10, 30);

        CourseRegistration registration = createRegistration(
                RegistrationStatus.CANCELLED, cancelledAt);

        when(registrationService.cancelRegistration(100L))
                .thenReturn(registration);

        mockMvc.perform(patch("/api/registrations/100/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(100))
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancelledAt").exists());

        verify(registrationService).cancelRegistration(100L);
    }

    @Test
    void shouldRejectRequestWithMissingStudentId() throws Exception {
        mockMvc.perform(post("/api/registrations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "courseOfferingId": 10
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(registrationService, never())
                .registerStudent(anyLong(), anyLong());
    }

    private CourseRegistration createRegistration(
            RegistrationStatus status,
            LocalDateTime cancelledAt) {

        Student student = mock(Student.class);
        CourseOffering offering = mock(CourseOffering.class);
        CourseRegistration registration = mock(CourseRegistration.class);

        when(registration.getId()).thenReturn(100L);
        when(registration.getStudent()).thenReturn(student);
        when(student.getId()).thenReturn(1L);
        when(registration.getCourseOffering()).thenReturn(offering);
        when(offering.getId()).thenReturn(10L);
        when(registration.getRegisteredAt())
                .thenReturn(LocalDateTime.of(2026, 10, 28, 10, 0));
        when(registration.getStatus()).thenReturn(status);
        when(registration.getCancelledAt()).thenReturn(cancelledAt);

        return registration;
    }
}
