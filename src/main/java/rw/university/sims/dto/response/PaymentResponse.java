package rw.university.sims.dto.response;
import rw.university.sims.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
public record PaymentResponse(Long id, Long studentId, String registrationNumber, Long semesterId,
 Long feeObligationId, BigDecimal amount, String currency, String transactionReference, PaymentStatus status,
 OffsetDateTime receivedAt, OffsetDateTime confirmedAt, Long confirmedBy, Long recordedBy, Long version) {}
