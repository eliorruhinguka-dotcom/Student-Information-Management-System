package rw.university.sims.service;

import rw.university.sims.entity.Payment;

import org.junit.jupiter.api.Test;
import rw.university.sims.dto.request.FeeObligationCreateRequest;
import rw.university.sims.entity.Role;
import rw.university.sims.entity.Semester;
import rw.university.sims.entity.Student;
import rw.university.sims.entity.UserAccount;
import rw.university.sims.enums.FeeType;
import rw.university.sims.exception.ResourceConflictException;
import rw.university.sims.repository.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FinanceServiceTest {
    @Test void financeOfficerCanCreateFeeObligation(){
        FeeObligationRepository obligations=mock(FeeObligationRepository.class);
        PaymentRepository payments=mock(PaymentRepository.class);
        PaymentStatusHistoryRepository history=mock(PaymentStatusHistoryRepository.class);
        StudentRepository students=mock(StudentRepository.class);
        SemesterRepository semesters=mock(SemesterRepository.class);
        UserAccountRepository users=mock(UserAccountRepository.class);
        FeeBalanceCalculator calculator=mock(FeeBalanceCalculator.class);
        AuditService audit=mock(AuditService.class);

        UserAccount actor=new UserAccount(); actor.setId(5L);
        Role role=new Role(); role.setName("FINANCE_OFFICER"); actor.setRoles(Set.of(role));
        Student student=new Student(10L,"UGL-2026-000010","Aline","Uwase","aline@example.com");
        Semester semester=mock(Semester.class);
        when(semester.getId()).thenReturn(20L); when(semester.getName()).thenReturn("Semester 1");
        when(users.findWithDetailsById(5L)).thenReturn(Optional.of(actor));
        when(students.findDetailedById(10L)).thenReturn(Optional.of(student));
        when(semesters.findById(20L)).thenReturn(Optional.of(semester));
        when(obligations.existsByStudent_IdAndSemester_IdAndFeeType(10L,20L,FeeType.TUITION)).thenReturn(false);
        when(obligations.saveAndFlush(any())).thenAnswer(invocation->invocation.getArgument(0));

        FinanceService service=new FinanceService(obligations,payments,history,students,semesters,users,calculator,audit);
        var response=service.createObligation(new FeeObligationCreateRequest(10L,20L,FeeType.TUITION,
                new BigDecimal("250000.00"),"RWF",true),5L,"127.0.0.1");

        assertEquals(10L,response.studentId());
        assertEquals(20L,response.semesterId());
        assertEquals(new BigDecimal("250000.00"),response.amountDue());
        assertEquals("RWF",response.currency());
        verify(audit).record(actor,"FEE_OBLIGATION_CREATED","FEE_OBLIGATION",null,"SUCCESS","127.0.0.1",null);
    }

    @Test void duplicateFeeTypeForSameStudentAndSemesterIsRejected(){
        FeeObligationRepository obligations=mock(FeeObligationRepository.class);
        StudentRepository students=mock(StudentRepository.class);
        SemesterRepository semesters=mock(SemesterRepository.class);
        UserAccountRepository users=mock(UserAccountRepository.class);
        UserAccount actor=new UserAccount(); Role role=new Role(); role.setName("FINANCE_OFFICER"); actor.setRoles(Set.of(role));
        when(users.findWithDetailsById(5L)).thenReturn(Optional.of(actor));
        when(students.findDetailedById(10L)).thenReturn(Optional.of(new Student(10L,"UGL-10","A","B","a@b.com")));
        Semester semester = mock(Semester.class);
        when(semester.getId()).thenReturn(20L);
        when(semesters.findById(20L)).thenReturn(Optional.of(semester));
        when(obligations.existsByStudent_IdAndSemester_IdAndFeeType(10L,20L,FeeType.TUITION)).thenReturn(true);
        FinanceService service=new FinanceService(obligations,mock(PaymentRepository.class),mock(PaymentStatusHistoryRepository.class),
                students,semesters,users,mock(FeeBalanceCalculator.class),mock(AuditService.class));
        assertThrows(ResourceConflictException.class,()->service.createObligation(new FeeObligationCreateRequest(10L,20L,
                FeeType.TUITION,new BigDecimal("500"),"RWF",true),5L,null));
    }
    @Test void sameTransactionReferenceAndSameRequestReturnsTheOriginalPayment(){
        FeeObligationRepository obligations=mock(FeeObligationRepository.class);
        PaymentRepository payments=mock(PaymentRepository.class);
        UserAccountRepository users=mock(UserAccountRepository.class);
        UserAccount actor=new UserAccount(); actor.setId(5L);
        Role role=new Role(); role.setName("FINANCE_OFFICER"); actor.setRoles(Set.of(role));
        when(users.findWithDetailsById(5L)).thenReturn(Optional.of(actor));
        Student student=new Student(10L,"UGL-10","A","B","a@b.com");
        Semester semester=mock(Semester.class); when(semester.getId()).thenReturn(20L);
        Payment original=mock(Payment.class);
        String canonical="10|20|null|50|RWF|TX-100|AUTO";
        String fingerprint;
        try {
            byte[] digest=java.security.MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            fingerprint=java.util.HexFormat.of().formatHex(digest);
        } catch(Exception e) { throw new AssertionError(e); }
        when(original.getRequestFingerprint()).thenReturn(fingerprint);
        when(original.getId()).thenReturn(44L); when(original.getStudent()).thenReturn(student);
        when(original.getSemester()).thenReturn(semester); when(original.getFeeObligation()).thenReturn(null);
        when(original.getAmount()).thenReturn(new BigDecimal("50.00")); when(original.getCurrency()).thenReturn("RWF");
        when(original.getTransactionReference()).thenReturn("TX-100"); when(original.getStatus()).thenReturn(rw.university.sims.enums.PaymentStatus.PENDING);
        when(original.getReceivedAt()).thenReturn(java.time.OffsetDateTime.parse("2026-11-01T10:00:00Z"));
        when(original.getConfirmedBy()).thenReturn(null); when(original.getRecordedBy()).thenReturn(actor); when(original.getVersion()).thenReturn(0L);
        when(payments.findByTransactionReference("TX-100")).thenReturn(Optional.of(original));
        FinanceService service=new FinanceService(obligations,payments,mock(PaymentStatusHistoryRepository.class),
                mock(StudentRepository.class),mock(SemesterRepository.class),users,mock(FeeBalanceCalculator.class),mock(AuditService.class));
        var outcome=service.recordPayment(new rw.university.sims.dto.request.PaymentCreateRequest(10L,20L,null,
                new BigDecimal("50.00"),null,"TX-100",null),5L,null);
        assertFalse(outcome.created());
        assertEquals(44L,outcome.payment().id());
        verify(payments,never()).saveAndFlush(any());
    }

    @Test void reusingTransactionReferenceWithDifferentDetailsIsRejected(){
        PaymentRepository payments=mock(PaymentRepository.class);
        UserAccountRepository users=mock(UserAccountRepository.class);
        UserAccount actor=new UserAccount(); Role role=new Role(); role.setName("FINANCE_OFFICER"); actor.setRoles(Set.of(role));
        when(users.findWithDetailsById(5L)).thenReturn(Optional.of(actor));
        Payment original=mock(Payment.class); when(original.getRequestFingerprint()).thenReturn("different-fingerprint");
        when(payments.findByTransactionReference("TX-100")).thenReturn(Optional.of(original));
        FinanceService service=new FinanceService(mock(FeeObligationRepository.class),payments,mock(PaymentStatusHistoryRepository.class),
                mock(StudentRepository.class),mock(SemesterRepository.class),users,mock(FeeBalanceCalculator.class),mock(AuditService.class));
        assertThrows(ResourceConflictException.class,()->service.recordPayment(new rw.university.sims.dto.request.PaymentCreateRequest(
                10L,20L,null,new BigDecimal("50.00"),null,"TX-100",null),5L,null));
    }

}
