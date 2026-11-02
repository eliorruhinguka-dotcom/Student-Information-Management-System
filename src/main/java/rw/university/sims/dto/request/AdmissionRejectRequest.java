package rw.university.sims.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdmissionRejectRequest(
        @NotBlank @Size(max = 2000) String reason,
        @jakarta.validation.constraints.NotNull @jakarta.validation.constraints.PositiveOrZero Long version) {}
