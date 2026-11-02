package rw.university.sims.dto.response;
import rw.university.sims.enums.PaymentStatus;
import java.time.OffsetDateTime;
public record PaymentStatusHistoryResponse(Long id, PaymentStatus fromStatus, PaymentStatus toStatus,
 Long changedBy, String changedByName, String reason, OffsetDateTime changedAt) {}
