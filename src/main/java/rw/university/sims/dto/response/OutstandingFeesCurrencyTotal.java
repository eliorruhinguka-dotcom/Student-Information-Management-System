package rw.university.sims.dto.response;

import java.math.BigDecimal;

public record OutstandingFeesCurrencyTotal(
        String currency,
        BigDecimal totalDue,
        BigDecimal totalPaid,
        BigDecimal outstandingBalance) {}
