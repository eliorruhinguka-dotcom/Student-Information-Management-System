package rw.university.sims.dto.response;

import rw.university.sims.enums.StudentStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record StudentProfileResponse(
        Long id, String registrationNumber, String firstName, String lastName,
        String email, LocalDate dateOfBirth, String nationalId,
        Long programmeId, String programmeCode, String programmeName,
        Long departmentId, String departmentName, StudentStatus status,
        OffsetDateTime admittedAt, Long version) {}
