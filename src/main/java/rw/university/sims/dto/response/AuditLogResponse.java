package rw.university.sims.dto.response;

import java.time.OffsetDateTime;
import java.util.Map;

public record AuditLogResponse(
    Long id,
    OffsetDateTime occurredAt,
    Long actorUserId,
    String action,
    String entityType,
    Long entityId,
    Map<String, Object> oldValue,
    Map<String, Object> newValue,
    String outcome,
    String ipAddress,
    String correlationId
) {
}