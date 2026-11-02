package rw.university.sims.dto.request;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
public record ExamCardGenerateRequest(@NotNull @Positive Long courseRegistrationId) {}
