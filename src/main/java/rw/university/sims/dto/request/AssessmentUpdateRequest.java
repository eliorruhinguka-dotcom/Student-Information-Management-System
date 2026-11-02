package rw.university.sims.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import rw.university.sims.enums.AssessmentType;
import java.math.BigDecimal;

public record AssessmentUpdateRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull AssessmentType type,
        @NotNull @DecimalMin("0.01") @DecimalMax("999.99") BigDecimal maxMark,
        @NotNull @DecimalMin("0.01") @DecimalMax("100.00") BigDecimal weightPercent) {}
