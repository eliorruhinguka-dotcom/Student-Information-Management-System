package rw.university.sims.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record OfferingUpdateRequest(

        @NotNull
        @Positive
        Integer capacity,

        @NotNull
        @Positive
        Long lecturerId,

        @NotNull
        @PositiveOrZero
        Long version

) {
}