package rw.university.sims.dto.response;
import rw.university.sims.enums.ExamCardStatus;
import java.time.OffsetDateTime;
public record ExamCardVerificationResponse(String cardNumber, boolean valid, ExamCardStatus status,
 String registrationNumber, String studentName, String courseCode, String courseTitle,
 String section, String semesterName, OffsetDateTime issuedAt) {}
