package rw.university.sims.dto.request;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record PaymentReverseRequest(@NotBlank @Size(max=255) String reason) {}
