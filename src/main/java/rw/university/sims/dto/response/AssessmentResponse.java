package rw.university.sims.dto.response;

import rw.university.sims.enums.AssessmentType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AssessmentResponse(
        Long id, Long courseOfferingId, String name, AssessmentType type,
        BigDecimal maxMark, BigDecimal weightPercent, OffsetDateTime createdAt) {}
