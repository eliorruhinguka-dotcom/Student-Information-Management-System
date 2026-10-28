package rw.university.sims.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CourseRegistrationRequest(

        @NotNull(message = "Student ID is required")
        @Positive(message = "Student ID must be positive")
        Long studentId,

        @NotNull(message = "Course offering ID is required")
        @Positive(message = "Course offering ID must be positive")
        Long courseOfferingId

) {
}