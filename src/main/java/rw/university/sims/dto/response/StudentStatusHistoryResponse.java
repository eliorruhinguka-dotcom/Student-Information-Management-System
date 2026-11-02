package rw.university.sims.dto.response;

import java.time.OffsetDateTime;

public record StudentStatusHistoryResponse(
        Long id, String fromStatus, String toStatus, String reason,
        Long changedById, String changedByName, OffsetDateTime changedAt) {}
