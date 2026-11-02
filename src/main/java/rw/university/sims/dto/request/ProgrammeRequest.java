package rw.university.sims.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProgrammeRequest(

        @NotBlank(message = "Programme code is required")
        @Size(max = 20, message = "Programme code cannot exceed 20 characters")
        String code,

        @NotBlank(message = "Programme name is required")
        @Size(max = 150, message = "Programme name cannot exceed 150 characters")
        String name,

        @NotNull(message = "Department ID is required")
        @Positive(message = "Department ID must be positive")
        Long departmentId,

        @NotNull(message = "Duration is required")
        @Positive(message = "Duration must be greater than zero")
        Short durationYears

) {
}