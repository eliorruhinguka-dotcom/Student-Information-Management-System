package rw.university.sims.dto.request;
import jakarta.validation.constraints.*;
import rw.university.sims.enums.FeeType;
import java.math.BigDecimal;
public record FeeObligationCreateRequest(@NotNull @Positive Long studentId, @NotNull @Positive Long semesterId,
 @NotNull FeeType feeType, @NotNull @DecimalMin("0.01") BigDecimal amountDue,
 @Pattern(regexp="[A-Za-z]{3}") String currency, Boolean countsTowardEligibility) {}
