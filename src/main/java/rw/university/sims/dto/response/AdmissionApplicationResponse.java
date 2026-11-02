package rw.university.sims.dto.response;

import rw.university.sims.enums.ApplicationStatus;
import java.time.LocalDate;
import java.time.OffsetDateTime;

public record AdmissionApplicationResponse(
        Long id, String reference, String firstName, String lastName, String email,
        LocalDate dateOfBirth, String nationalId, Long programmeId, String programmeName,
        ApplicationStatus status, OffsetDateTime submittedAt, OffsetDateTime reviewedAt,
        String decisionReason, Long studentId, Long version) {}
