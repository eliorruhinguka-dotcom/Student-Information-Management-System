package rw.university.sims.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.university.sims.dto.request.AdmissionApplicationCreateRequest;
import rw.university.sims.enums.ApplicationStatus;
import rw.university.sims.exception.ResourceConflictException;
import rw.university.sims.repository.*;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdmissionApplicationServiceTest {
    @Mock AdmissionApplicationRepository applicationRepository;
    @Mock ProgrammeRepository programmeRepository;
    @Mock StudentRepository studentRepository;
    @Mock StudentStatusHistoryRepository historyRepository;
    @Mock UserAccountRepository userRepository;
    @Mock AuditService auditService;
    @Mock AdmissionRateLimiter rateLimiter;
    @InjectMocks AdmissionApplicationService service;

    @Test
    void rejectsAnEmailThatAlreadyHasAnOpenApplication() {
        AdmissionApplicationCreateRequest request = new AdmissionApplicationCreateRequest(
                "Amina", "Test", "amina@example.com", LocalDate.of(2005, 5, 1), null, 3L);
        when(studentRepository.existsByEmailIgnoreCase("amina@example.com")).thenReturn(false);
        when(applicationRepository.existsByEmailIgnoreCaseAndStatus("amina@example.com", ApplicationStatus.SUBMITTED))
                .thenReturn(true);
        assertThrows(ResourceConflictException.class, () -> service.submit(request, "127.0.0.1"));
    }
}
