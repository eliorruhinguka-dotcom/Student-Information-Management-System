package rw.university.sims.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ResultCorrectionRequest(
        @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal correctedMark,
        @NotBlank @Size(max = 2000) String reason) {}
