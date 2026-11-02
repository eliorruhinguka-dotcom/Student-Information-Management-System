package rw.university.sims.dto.response;

import java.math.BigDecimal;

public record OutstandingFeesStudentRow(
        Long studentId,
        String registrationNumber,
        String firstName,
        String lastName,
        String email,
        String currency,
        BigDecimal totalDue,
        BigDecimal totalPaid,
        BigDecimal outstandingBalance,
        BigDecimal percentPaid) {}
