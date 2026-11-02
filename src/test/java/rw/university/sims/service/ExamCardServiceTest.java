package rw.university.sims.service;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import rw.university.sims.entity.*;
import rw.university.sims.enums.*;
import rw.university.sims.repository.*;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExamCardServiceTest {
    @Test void unpaidStudentGetsARefusedAttemptRecordedBeforeTheRequestFails(){
        ExamCardRepository cards=mock(ExamCardRepository.class);
        ExamCardAttemptRepository attempts=mock(ExamCardAttemptRepository.class);
        CourseRegistrationRepository registrations=mock(CourseRegistrationRepository.class);
        StudentRepository students=mock(StudentRepository.class);
        UserAccountRepository users=mock(UserAccountRepository.class);
        FeeBalanceCalculator balances=mock(FeeBalanceCalculator.class);
        ExamCardAttemptRecorder recorder=mock(ExamCardAttemptRecorder.class);
        ExamCardWriter writer=mock(ExamCardWriter.class);
        AuditService audit=mock(AuditService.class);

        Role studentRole=new Role(); studentRole.setName("STUDENT");
        UserAccount actor=mock(UserAccount.class);
        when(actor.getId()).thenReturn(1L); when(actor.getRoles()).thenReturn(Set.of(studentRole));
        Student student=new Student(10L,"UGL-2026-000010","Aline","Uwase","aline@example.com");
        StudentUserLink link=mock(StudentUserLink.class);
        when(link.getStudent()).thenReturn(student); when(actor.getStudentLink()).thenReturn(link);
        when(users.findWithDetailsById(1L)).thenReturn(Optional.of(actor));
        when(students.findDetailedById(10L)).thenReturn(Optional.of(student));

        CourseRegistration registration=mock(CourseRegistration.class);
        when(registration.getId()).thenReturn(30L);
        CourseOffering offering=mock(CourseOffering.class);
        Semester semester=mock(Semester.class);
        when(registrations.findDetailedById(30L)).thenReturn(Optional.of(registration));
        when(registration.getStudent()).thenReturn(student);
        when(registration.getStatus()).thenReturn(RegistrationStatus.REGISTERED);
        when(registration.getCourseOffering()).thenReturn(offering);
        when(offering.getSemester()).thenReturn(semester);
        when(semester.getId()).thenReturn(20L);
        when(balances.calculate(10L,20L)).thenReturn(new rw.university.sims.dto.response.FeeBalanceResponse(
                10L,"UGL-2026-000010",20L,new BigDecimal("1000.00"),new BigDecimal("500.00"),
                new BigDecimal("500.00"),new BigDecimal("50.00"),true,false));

        ExamCardService service=new ExamCardService(cards,attempts,registrations,students,users,balances,recorder,writer,audit);
        ResponseStatusException error=assertThrows(ResponseStatusException.class,()->service.generate(30L,1L,"127.0.0.1"));
        assertEquals(422,error.getStatusCode().value());
        verify(recorder).record(10L,30L,null,ExamCardAttemptOutcome.REFUSED,"FEES_NOT_PAID",1L,"127.0.0.1");
        verify(writer,never()).create(any(),any(),any(),any(),any());
    }
}
