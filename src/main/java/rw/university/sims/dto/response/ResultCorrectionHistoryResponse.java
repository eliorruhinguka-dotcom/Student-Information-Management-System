package rw.university.sims.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ResultCorrectionHistoryResponse(
        Long id,
        Long resultId,
        BigDecimal originalMark,
        BigDecimal correctedMark,
        String originalGrade,
        String correctedGrade,
        String reason,
        String requestStatus,
        Long requestedById,
        String requestedByName,
        Long authorisedById,
        String authorisedByName,
        OffsetDateTime recordedAt) {}
