package rw.university.sims.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record AssessmentScoreEntry(
        @NotNull @Positive Long courseRegistrationId,
        @NotNull @DecimalMin("0.00") BigDecimal mark) {}
