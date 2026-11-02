package rw.university.sims.entity;

import jakarta.persistence.*;
import rw.university.sims.enums.ExamCardAttemptOutcome;
import java.time.OffsetDateTime;

@Entity
@Table(name = "exam_card_attempt")
public class ExamCardAttempt {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "student_id", nullable = false) private Student student;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "course_registration_id") private CourseRegistration courseRegistration;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "exam_card_id") private ExamCard examCard;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10) private ExamCardAttemptOutcome outcome;
    @Column(name = "refusal_reason", length = 40) private String refusalReason;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "requested_by", nullable = false) private UserAccount requestedBy;
    @Column(name = "ip_address", length = 45) private String ipAddress;
    @Column(name = "requested_at", nullable = false, insertable = false, updatable = false) private OffsetDateTime requestedAt;
    protected ExamCardAttempt() {}
    public ExamCardAttempt(Student student, CourseRegistration registration, ExamCard card, ExamCardAttemptOutcome outcome,
                           String refusalReason, UserAccount requestedBy, String ipAddress){
        this.student=student; this.courseRegistration=registration; this.examCard=card; this.outcome=outcome;
        this.refusalReason=refusalReason; this.requestedBy=requestedBy; this.ipAddress=ipAddress;
    }
    public Long getId(){return id;} public Student getStudent(){return student;} public CourseRegistration getCourseRegistration(){return courseRegistration;}
    public ExamCard getExamCard(){return examCard;} public ExamCardAttemptOutcome getOutcome(){return outcome;} public String getRefusalReason(){return refusalReason;}
    public UserAccount getRequestedBy(){return requestedBy;} public String getIpAddress(){return ipAddress;} public OffsetDateTime getRequestedAt(){return requestedAt;}
}
