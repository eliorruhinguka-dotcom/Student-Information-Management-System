package rw.university.sims.dto.response;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AssessmentScoreResponse(
        Long id, Long assessmentId, Long courseRegistrationId,
        Long studentId, String registrationNumber, String firstName,
        String lastName, BigDecimal mark, Long enteredById,
        OffsetDateTime enteredAt, OffsetDateTime updatedAt) {}
