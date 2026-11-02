package rw.university.sims.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AdmissionApprovalRequest(@NotNull @PositiveOrZero Long version) {}
