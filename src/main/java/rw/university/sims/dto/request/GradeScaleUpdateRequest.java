package rw.university.sims.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
public record GradeScaleUpdateRequest(@NotBlank @Size(max=100) String name, @NotNull LocalDate effectiveFrom,
 @NotEmpty List<@Valid GradeScaleBandRequest> bands) {}
