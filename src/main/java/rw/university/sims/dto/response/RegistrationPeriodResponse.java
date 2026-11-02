package rw.university.sims.dto.response;

import rw.university.sims.enums.RegistrationPeriodStatus;

import java.time.OffsetDateTime;

public record RegistrationPeriodResponse(
    Long id,
    Long semesterId,
    OffsetDateTime opensAt,
    OffsetDateTime closesAt,
    RegistrationPeriodStatus status,
    Long openedBy,
    Long closedBy,
    OffsetDateTime closedAt
) {
}