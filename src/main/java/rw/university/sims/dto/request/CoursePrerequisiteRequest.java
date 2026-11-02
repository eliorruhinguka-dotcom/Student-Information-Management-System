package rw.university.sims.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CoursePrerequisiteRequest(
        @NotNull(message = "Prerequisite course ID is required")
        @Positive(message = "Prerequisite course ID must be positive")
        Long prerequisiteCourseId
) {
}