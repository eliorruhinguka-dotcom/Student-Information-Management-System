package rw.university.sims.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "assessment_score", uniqueConstraints = @UniqueConstraint(
        name = "uq_assessment_score_registration", columnNames = {"assessment_id", "course_registration_id"}))
public class AssessmentScore {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_registration_id", nullable = false)
    private CourseRegistration courseRegistration;
    @Column(precision = 5, scale = 2)
    private BigDecimal mark;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "entered_by", nullable = false)
    private UserAccount enteredBy;
    @Column(name = "entered_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime enteredAt;
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    protected AssessmentScore() {}
    public AssessmentScore(Assessment assessment, CourseRegistration registration,
                           BigDecimal mark, UserAccount actor) {
        this.assessment = assessment; this.courseRegistration = registration;
        this.mark = mark; this.enteredBy = actor;
    }
    public Long getId() { return id; }
    public Assessment getAssessment() { return assessment; }
    public CourseRegistration getCourseRegistration() { return courseRegistration; }
    public BigDecimal getMark() { return mark; }
    public void setMark(BigDecimal mark) { this.mark = mark; }
    public UserAccount getEnteredBy() { return enteredBy; }
    public void setEnteredBy(UserAccount enteredBy) { this.enteredBy = enteredBy; }
    public OffsetDateTime getEnteredAt() { return enteredAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
}
