package rw.university.sims.entity;

import jakarta.persistence.*;
import rw.university.sims.enums.ExamCardStatus;
import java.time.OffsetDateTime;

@Entity
@Table(name = "exam_card")
public class ExamCard {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "course_registration_id", nullable = false, unique = true) private CourseRegistration courseRegistration;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "student_id", nullable = false) private Student student;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "semester_id", nullable = false) private Semester semester;
    @Column(name = "card_number", nullable = false, unique = true, length = 50) private String cardNumber;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 10) private ExamCardStatus status = ExamCardStatus.ISSUED;
    @Column(name = "issued_at", nullable = false) private OffsetDateTime issuedAt;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "requested_by", nullable = false) private UserAccount requestedBy;
    protected ExamCard() {}
    public ExamCard(CourseRegistration registration, Student student, Semester semester, String cardNumber, UserAccount requester){
        this.courseRegistration=registration; this.student=student; this.semester=semester; this.cardNumber=cardNumber;
        this.requestedBy=requester; this.issuedAt=OffsetDateTime.now(); this.status=ExamCardStatus.ISSUED;
    }
    public Long getId(){return id;} public CourseRegistration getCourseRegistration(){return courseRegistration;} public Student getStudent(){return student;}
    public Semester getSemester(){return semester;} public String getCardNumber(){return cardNumber;} public ExamCardStatus getStatus(){return status;}
    public OffsetDateTime getIssuedAt(){return issuedAt;} public UserAccount getRequestedBy(){return requestedBy;}
}
