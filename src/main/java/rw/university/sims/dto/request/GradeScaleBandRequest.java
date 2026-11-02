package rw.university.sims.dto.request;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record GradeScaleBandRequest(@NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal minMark,
 @NotNull @DecimalMin("0.00") @DecimalMax("100.00") BigDecimal maxMark,
 @NotBlank @Size(max=3) String grade, @NotNull @DecimalMin("0.00") BigDecimal gradePoints, boolean pass) {}
