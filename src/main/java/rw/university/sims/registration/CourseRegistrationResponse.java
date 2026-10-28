package rw.university.sims.registration;

import java.time.LocalDateTime;

public record CourseRegistrationResponse(
        Long id,
        Long studentId,
        Long courseOfferingId,
        LocalDateTime registeredAt
) {
}