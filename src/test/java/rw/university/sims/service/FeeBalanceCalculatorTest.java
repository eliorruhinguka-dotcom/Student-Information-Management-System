package rw.university.sims.service;

import org.junit.jupiter.api.Test;
import rw.university.sims.entity.Student;
import rw.university.sims.enums.StudentStatus;
import rw.university.sims.repository.FeeObligationRepository;
import rw.university.sims.repository.PaymentRepository;
import rw.university.sims.repository.StudentRepository;
import java.math.BigDecimal;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FeeBalanceCalculatorTest {
    @Test void thirtyPercentPaymentAllowsRegistrationButNotExamCard(){
        FeeObligationRepository obligations=mock(FeeObligationRepository.class);
        PaymentRepository payments=mock(PaymentRepository.class);
        StudentRepository students=mock(StudentRepository.class);
        Student student=new Student(10L,"UGL-2026-000010","Ada","Mutesi","ada@example.com");
        student.setStatus(StudentStatus.ACTIVE);
        when(students.findDetailedById(10L)).thenReturn(Optional.of(student));
        when(obligations.sumEligibleDue(10L,20L)).thenReturn(new BigDecimal("1000.00"));
        when(payments.sumConfirmed(10L,20L)).thenReturn(new BigDecimal("300.00"));
        var result=new FeeBalanceCalculator(obligations,payments,students).calculate(10L,20L);
        assertEquals(new BigDecimal("1000.00"),result.totalDue());
        assertEquals(new BigDecimal("300.00"),result.totalPaid());
        assertEquals(new BigDecimal("700.00"),result.outstandingBalance());
        assertEquals(new BigDecimal("30.00"),result.percentagePaid());
        assertTrue(result.canRegister());
        assertFalse(result.canSitExams());
    }
    @Test void zeroEligibleObligationsNeverAllowsRegistrationOrExamCard(){
        FeeObligationRepository obligations=mock(FeeObligationRepository.class);
        PaymentRepository payments=mock(PaymentRepository.class);
        StudentRepository students=mock(StudentRepository.class);
        when(students.findDetailedById(10L)).thenReturn(Optional.of(new Student(10L,"UGL-10","Ada","M","a@b.com")));
        when(obligations.sumEligibleDue(10L,20L)).thenReturn(BigDecimal.ZERO);
        when(payments.sumConfirmed(10L,20L)).thenReturn(new BigDecimal("500.00"));
        var result=new FeeBalanceCalculator(obligations,payments,students).calculate(10L,20L);
        assertFalse(result.canRegister());
        assertFalse(result.canSitExams());
        assertEquals(BigDecimal.ZERO.setScale(2),result.percentagePaid());
    }
}
