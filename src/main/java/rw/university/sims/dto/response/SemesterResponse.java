package rw.university.sims.dto.response;

import java.time.LocalDate;

public record SemesterResponse(
    Long id,
    String name,
    String academicYear,
    LocalDate startDate,
    LocalDate endDate,
    Short maxCreditLoad
) {
}