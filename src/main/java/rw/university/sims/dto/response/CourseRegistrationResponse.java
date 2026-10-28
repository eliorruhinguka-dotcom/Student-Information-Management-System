package rw.university.sims.dto.response;

import rw.university.sims.enums.RegistrationStatus;

import java.time.LocalDateTime;

public record CourseRegistrationResponse(
        Long id,
        Long studentId,
        Long courseOfferingId,
        LocalDateTime registeredAt,
        RegistrationStatus status,
        LocalDateTime cancelledAt
) {
}