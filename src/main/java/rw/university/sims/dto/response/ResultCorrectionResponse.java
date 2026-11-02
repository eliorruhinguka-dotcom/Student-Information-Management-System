package rw.university.sims.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ResultCorrectionResponse(
        Long id, Long resultId, BigDecimal originalMark, BigDecimal correctedMark,
        String originalGrade, String correctedGrade, String reason,
        Long authorisedById, String authorisedByName, OffsetDateTime correctedAt) {}
