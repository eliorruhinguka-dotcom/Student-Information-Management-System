package rw.university.sims.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record OfferingCreateRequest(

        @NotNull
        @Positive
        Long courseId,

        @NotNull
        @Positive
        Long semesterId,

        @NotBlank
        @Size(max = 20)
        String section,

        @NotNull
        @Positive
        Long lecturerId,

        @NotNull
        @Positive
        Integer capacity

) {
}