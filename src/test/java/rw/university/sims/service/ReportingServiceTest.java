package rw.university.sims.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import rw.university.sims.dto.response.*;
import rw.university.sims.entity.Department;
import rw.university.sims.entity.Role;
import rw.university.sims.entity.UserAccount;
import rw.university.sims.repository.CourseOfferingRepository;
import rw.university.sims.repository.ReportQueryRepository;
import rw.university.sims.repository.UserAccountRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReportingServiceTest {
    @Test
    void lecturerEnrolmentReportIsRestrictedToOwnOfferings() {
        ReportQueryRepository reports = mock(ReportQueryRepository.class);
        UserAccountRepository users = mock(UserAccountRepository.class);
        CourseOfferingRepository offerings = mock(CourseOfferingRepository.class);
        UserAccount lecturer = userWithRole("LECTURER");
        when(users.findWithDetailsById(15L)).thenReturn(Optional.of(lecturer));
        List<EnrolmentByProgrammeRow> expected = List.of(new EnrolmentByProgrammeRow(
                3L, "CS", "Computer Science", 2L, "ICT", "Information Technology", 12, 4, 8));
        when(reports.enrolmentByProgramme(2027L, null, 15L)).thenReturn(expected);

        ReportingService service = new ReportingService(reports, users, offerings);
        assertEquals(expected, service.enrolmentByProgramme(2027L, 15L));
        verify(reports).enrolmentByProgramme(2027L, null, 15L);
    }

    @Test
    void financeReportTotalsAreKeptSeparateByCurrency() {
        ReportQueryRepository reports = mock(ReportQueryRepository.class);
        UserAccountRepository users = mock(UserAccountRepository.class);
        CourseOfferingRepository offerings = mock(CourseOfferingRepository.class);
        when(users.findWithDetailsById(16L)).thenReturn(Optional.of(userWithRole("FINANCE_OFFICER")));
        when(reports.outstandingFees(2027L)).thenReturn(List.of(
                new OutstandingFeesStudentRow(1L, "UGL-1", "A", "One", "a@example.test", "RWF",
                        new BigDecimal("100.00"), new BigDecimal("40.00"), new BigDecimal("60.00"), new BigDecimal("40.00")),
                new OutstandingFeesStudentRow(2L, "UGL-2", "B", "Two", "b@example.test", "USD",
                        new BigDecimal("50.00"), new BigDecimal("20.00"), new BigDecimal("30.00"), new BigDecimal("40.00"))));

        ReportingService service = new ReportingService(reports, users, offerings);
        OutstandingFeesReportResponse response = service.outstandingFees(2027L, 16L);
        assertEquals(2, response.students().size());
        assertEquals(2, response.totalsByCurrency().size());
        assertEquals("RWF", response.totalsByCurrency().get(0).currency());
        assertEquals("USD", response.totalsByCurrency().get(1).currency());
    }

    @Test
    void studentCannotReadFinanceReport() {
        ReportQueryRepository reports = mock(ReportQueryRepository.class);
        UserAccountRepository users = mock(UserAccountRepository.class);
        CourseOfferingRepository offerings = mock(CourseOfferingRepository.class);
        when(users.findWithDetailsById(17L)).thenReturn(Optional.of(userWithRole("STUDENT")));
        ReportingService service = new ReportingService(reports, users, offerings);
        assertThrows(ResponseStatusException.class, () -> service.outstandingFees(2027L, 17L));
        verifyNoInteractions(reports);
    }

    private static UserAccount userWithRole(String name) {
        UserAccount account = new UserAccount();
        Role role = new Role();
        role.setName(name);
        account.setRoles(Set.of(role));
        return account;
    }
}
