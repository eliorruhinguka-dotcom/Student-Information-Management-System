package rw.university.sims.dto.request;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
public record PaymentCreateRequest(@NotNull @Positive Long studentId, @NotNull @Positive Long semesterId,
 @Positive Long feeObligationId, @NotNull @DecimalMin("0.01") BigDecimal amount,
 @Pattern(regexp="[A-Za-z]{3}") String currency, @NotBlank @Size(max=100) String transactionReference,
 OffsetDateTime receivedAt) {}
