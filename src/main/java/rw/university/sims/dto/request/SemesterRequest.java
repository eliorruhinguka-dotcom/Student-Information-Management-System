package rw.university.sims.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public class SemesterRequest {

    @NotBlank(message = "Semester name is required")
    @Size(max = 100, message = "Semester name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Academic year is required")
    @Pattern(
        regexp = "^\\d{4}/\\d{4}$",
        message = "Academic year must use the format YYYY/YYYY"
    )
    private String academicYear;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotNull(message = "Maximum credit load is required")
    @Positive(message = "Maximum credit load must be greater than zero")
    private Short maxCreditLoad;

    public SemesterRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Short getMaxCreditLoad() {
        return maxCreditLoad;
    }

    public void setMaxCreditLoad(Short maxCreditLoad) {
        this.maxCreditLoad = maxCreditLoad;
    }
}