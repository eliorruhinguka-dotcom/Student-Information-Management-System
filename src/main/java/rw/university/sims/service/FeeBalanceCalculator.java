package rw.university.sims.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.university.sims.dto.response.FeeBalanceResponse;
import rw.university.sims.entity.Student;
import rw.university.sims.exception.ResourceNotFoundException;
import rw.university.sims.repository.FeeObligationRepository;
import rw.university.sims.repository.PaymentRepository;
import rw.university.sims.repository.StudentRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FeeBalanceCalculator {
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal THIRTY = new BigDecimal("30.00");
    private static final BigDecimal HUNDRED = new BigDecimal("100.00");
    private final FeeObligationRepository obligationRepository;
    private final PaymentRepository paymentRepository;
    private final StudentRepository studentRepository;
    public FeeBalanceCalculator(FeeObligationRepository obligationRepository, PaymentRepository paymentRepository,
                                StudentRepository studentRepository) {
        this.obligationRepository=obligationRepository; this.paymentRepository=paymentRepository; this.studentRepository=studentRepository;
    }
    @Transactional(readOnly = true)
    public FeeBalanceResponse calculate(Long studentId, Long semesterId) {
        Student student = studentRepository.findDetailedById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
        BigDecimal due = zero(obligationRepository.sumEligibleDue(studentId, semesterId));
        BigDecimal paid = zero(paymentRepository.sumConfirmed(studentId, semesterId));
        BigDecimal outstanding = due.subtract(paid).max(ZERO).setScale(2, RoundingMode.HALF_UP);
        BigDecimal percent = due.signum() == 0 ? ZERO : paid.multiply(HUNDRED).divide(due, 2, RoundingMode.HALF_UP).min(HUNDRED);
        boolean active = student.getStatus() == rw.university.sims.enums.StudentStatus.ACTIVE;
        boolean hasDue = due.signum() > 0;
        boolean register = active && hasDue && percent.compareTo(THIRTY) >= 0;
        boolean exams = active && hasDue && paid.compareTo(due) >= 0;
        return new FeeBalanceResponse(student.getId(), student.getRegistrationNumber(), semesterId,
                due.setScale(2, RoundingMode.HALF_UP), paid.setScale(2, RoundingMode.HALF_UP), outstanding, percent, register, exams);
    }
    private BigDecimal zero(BigDecimal value) { return value == null ? ZERO : value; }
}
