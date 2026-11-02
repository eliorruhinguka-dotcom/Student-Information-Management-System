package rw.university.sims.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.OffsetDateTime;

public class RegistrationPeriodRequest {

    @NotNull(message = "Opening date and time are required")
    private OffsetDateTime opensAt;

    @NotNull(message = "Closing date and time are required")
    private OffsetDateTime closesAt;

    public RegistrationPeriodRequest() {
    }

    public OffsetDateTime getOpensAt() {
        return opensAt;
    }

    public void setOpensAt(OffsetDateTime opensAt) {
        this.opensAt = opensAt;
    }

    public OffsetDateTime getClosesAt() {
        return closesAt;
    }

    public void setClosesAt(OffsetDateTime closesAt) {
        this.closesAt = closesAt;
    }
}