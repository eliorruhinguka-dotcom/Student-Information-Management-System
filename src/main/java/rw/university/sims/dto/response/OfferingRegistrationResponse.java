package rw.university.sims.dto.response;

import rw.university.sims.enums.RegistrationStatus;

import java.time.LocalDateTime;

public record OfferingRegistrationResponse(
        Long registrationId,
        Long studentId,
        String studentNumber,
        String firstName,
        String lastName,
        String email,
        RegistrationStatus status,
        LocalDateTime registeredAt,
        LocalDateTime cancelledAt
) {
}