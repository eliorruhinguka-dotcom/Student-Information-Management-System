package rw.university.sims.dto.response;

import rw.university.sims.enums.ResultStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record AcademicResultResponse(
        Long id, Long courseRegistrationId, Long studentId, String registrationNumber,
        String firstName, String lastName, Long courseOfferingId, String courseCode,
        String courseTitle, BigDecimal finalMark, String grade, Boolean passed,
        Long gradeScaleId, ResultStatus status, OffsetDateTime submittedAt,
        OffsetDateTime approvedAt, Long version) {}
