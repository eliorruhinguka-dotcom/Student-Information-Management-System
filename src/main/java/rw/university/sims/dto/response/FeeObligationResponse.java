package rw.university.sims.dto.response;
import rw.university.sims.enums.FeeObligationStatus;
import rw.university.sims.enums.FeeType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
public record FeeObligationResponse(Long id, Long studentId, String registrationNumber, String studentName,
 Long semesterId, String semesterName, FeeType feeType, BigDecimal amountDue, String currency,
 boolean countsTowardEligibility, FeeObligationStatus status, OffsetDateTime createdAt, Long createdBy) {}
