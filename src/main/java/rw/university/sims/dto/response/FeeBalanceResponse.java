package rw.university.sims.dto.response;
import java.math.BigDecimal;
public record FeeBalanceResponse(Long studentId, String registrationNumber, Long semesterId,
 BigDecimal totalDue, BigDecimal totalPaid, BigDecimal outstandingBalance, BigDecimal percentagePaid,
 boolean canRegister, boolean canSitExams) {}
