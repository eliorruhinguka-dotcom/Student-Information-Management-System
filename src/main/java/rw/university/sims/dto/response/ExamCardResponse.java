package rw.university.sims.dto.response;
import rw.university.sims.enums.ExamCardStatus;
import java.time.OffsetDateTime;
public record ExamCardResponse(Long id, String cardNumber, ExamCardStatus status, Long courseRegistrationId,
 String courseCode, String courseTitle, String section, Long semesterId, String semesterName,
 String registrationNumber, String studentName, OffsetDateTime issuedAt) {}
