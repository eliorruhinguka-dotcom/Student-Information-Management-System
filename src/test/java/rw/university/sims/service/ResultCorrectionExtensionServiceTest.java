package rw.university.sims.service;

import org.junit.jupiter.api.Test;
import rw.university.sims.dto.request.ResultCorrectionRequest;
import rw.university.sims.dto.response.ResultCorrectionHistoryResponse;
import rw.university.sims.entity.*;
import rw.university.sims.enums.ResultStatus;
import rw.university.sims.repository.AcademicResultRepository;
import rw.university.sims.repository.ResultCorrectionRepository;
import rw.university.sims.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ResultCorrectionExtensionServiceTest {
    @Test
    void lecturerRequestIsRecordedWithoutChangingPublishedResult() {
        AcademicResultRepository results = mock(AcademicResultRepository.class);
        ResultCorrectionRepository corrections = mock(ResultCorrectionRepository.class);
        UserAccountRepository users = mock(UserAccountRepository.class);
        AuditService audit = mock(AuditService.class);

        UserAccount lecturer = new UserAccount();
        lecturer.setId(15L);
        lecturer.setFullName("Assigned Lecturer");
        Role role = new Role();
        role.setName("LECTURER");
        lecturer.setRoles(Set.of(role));

        UserAccount assignedLecturer = new UserAccount();
        assignedLecturer.setId(15L);
        CourseOffering offering = mock(CourseOffering.class);
        when(offering.getLecturer()).thenReturn(assignedLecturer);
        CourseRegistration registration = new CourseRegistration(null, offering);
        AcademicResult result = new AcademicResult(registration);
        result.setFinalMark(new BigDecimal("70.00"));
        result.setGrade("C");
        result.setStatus(ResultStatus.PUBLISHED);
        GradeScale scale = new GradeScale();
        scale.addBand(new GradeScaleBand(new BigDecimal("0"), new BigDecimal("79.99"),
                "C", new BigDecimal("2.00"), true));
        scale.addBand(new GradeScaleBand(new BigDecimal("80"), new BigDecimal("100"),
                "A", new BigDecimal("4.00"), true));
        result.setGradeScale(scale);

        when(results.findByIdForUpdate(30L)).thenReturn(Optional.of(result));
        when(users.findWithDetailsById(15L)).thenReturn(Optional.of(lecturer));
        when(corrections.saveAndFlush(any(ResultCorrection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResultCorrectionExtensionService service = new ResultCorrectionExtensionService(
                results, corrections, users, audit);
        ResultCorrectionHistoryResponse response = service.request(
                30L, new ResultCorrectionRequest(new BigDecimal("85.00"), "Recheck calculation"),
                15L, "127.0.0.1");

        assertEquals("REQUESTED", response.requestStatus());
        assertEquals(new BigDecimal("70.00"), result.getFinalMark(),
                "A lecturer request must not change the published result itself.");
        verify(corrections).saveAndFlush(argThat(c ->
                "REQUESTED".equals(c.getRequestStatus()) && c.getAuthorisedBy() == null
                        && c.getRequestedBy() == lecturer));
        verify(audit).record(lecturer, "RESULT_CORRECTION_REQUESTED", "RESULT",
                30L, "SUCCESS", "127.0.0.1", null);
    }
}
