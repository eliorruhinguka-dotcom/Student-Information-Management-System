package rw.university.sims.dto.request;

import jakarta.validation.constraints.NotNull;

import rw.university.sims.enums.OfferingStatus;

public record OfferingStatusRequest(
        @NotNull
        OfferingStatus status
) {
}