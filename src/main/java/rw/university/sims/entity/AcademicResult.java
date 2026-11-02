package rw.university.sims.entity;

import jakarta.persistence.*;
import rw.university.sims.enums.ResultStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "result")
public class AcademicResult {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_registration_id", nullable = false, unique = true)
    private CourseRegistration courseRegistration;
    @Column(name = "final_mark", precision = 5, scale = 2)
    private BigDecimal finalMark;
    @Column(length = 3)
    private String grade;
    private Boolean passed;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grade_scale_id")
    private GradeScale gradeScale;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResultStatus status = ResultStatus.DRAFT;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by")
    private UserAccount submittedBy;
    @Column(name = "submitted_at")
    private OffsetDateTime submittedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private UserAccount approvedBy;
    @Column(name = "approved_at")
    private OffsetDateTime approvedAt;
    @Version
    @Column(nullable = false)
    private Long version = 0L;

    protected AcademicResult() {}
    public AcademicResult(CourseRegistration registration) { this.courseRegistration = registration; }
    public Long getId() { return id; }
    public CourseRegistration getCourseRegistration() { return courseRegistration; }
    public BigDecimal getFinalMark() { return finalMark; }
    public void setFinalMark(BigDecimal finalMark) { this.finalMark = finalMark; }
    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }
    public Boolean getPassed() { return passed; }
    public void setPassed(Boolean passed) { this.passed = passed; }
    public GradeScale getGradeScale() { return gradeScale; }
    public void setGradeScale(GradeScale gradeScale) { this.gradeScale = gradeScale; }
    public ResultStatus getStatus() { return status; }
    public void setStatus(ResultStatus status) { this.status = status; }
    public UserAccount getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(UserAccount submittedBy) { this.submittedBy = submittedBy; }
    public OffsetDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(OffsetDateTime submittedAt) { this.submittedAt = submittedAt; }
    public UserAccount getApprovedBy() { return approvedBy; }
    public void setApprovedBy(UserAccount approvedBy) { this.approvedBy = approvedBy; }
    public OffsetDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(OffsetDateTime approvedAt) { this.approvedAt = approvedAt; }
    public Long getVersion() { return version; }
}
