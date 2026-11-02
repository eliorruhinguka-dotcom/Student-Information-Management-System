package rw.university.sims.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CourseUpdateRequest(

        @NotBlank(message = "Course title is required")
        @Size(max = 150, message = "Course title cannot exceed 150 characters")
        String title,

        @NotNull(message = "Course credits are required")
        @Positive(message = "Course credits must be greater than zero")
        Integer credits,

        @NotNull(message = "Department ID is required")
        @Positive(message = "Department ID must be positive")
        Long departmentId

) {
}