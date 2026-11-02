package rw.university.sims.dto.response;
import rw.university.sims.enums.ExamCardAttemptOutcome;
import java.time.OffsetDateTime;
public record ExamCardAttemptResponse(Long id, Long courseRegistrationId, String cardNumber,
 ExamCardAttemptOutcome outcome, String refusalReason, Long requestedBy, String requestedByName,
 String ipAddress, OffsetDateTime requestedAt) {}
