package rw.university.sims.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import rw.university.sims.enums.FeeObligationStatus;
import rw.university.sims.enums.FeeType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "fee_obligation", uniqueConstraints = @UniqueConstraint(
        name = "uq_fee_obligation_student_semester_type", columnNames = {"student_id", "semester_id", "fee_type"}))
public class FeeObligation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "student_id", nullable = false) private Student student;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "semester_id", nullable = false) private Semester semester;
    @Enumerated(EnumType.STRING) @Column(name = "fee_type", nullable = false, length = 20) private FeeType feeType;
    @Column(name = "amount_due", nullable = false, precision = 12, scale = 2) private BigDecimal amountDue;
    @JdbcTypeCode(SqlTypes.CHAR) @Column(nullable = false, length = 3) private String currency = "RWF";
    @Column(name = "counts_toward_eligibility", nullable = false) private boolean countsTowardEligibility = true;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 12) private FeeObligationStatus status = FeeObligationStatus.ACTIVE;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "created_by", nullable = false) private UserAccount createdBy;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
    protected FeeObligation() {}
    public FeeObligation(Student student, Semester semester, FeeType feeType, BigDecimal amountDue,
                         String currency, boolean countsTowardEligibility, UserAccount createdBy) {
        this.student=student; this.semester=semester; this.feeType=feeType; this.amountDue=amountDue;
        this.currency=currency; this.countsTowardEligibility=countsTowardEligibility; this.createdBy=createdBy; this.createdAt=OffsetDateTime.now();
    }
    public Long getId(){return id;} public Student getStudent(){return student;} public Semester getSemester(){return semester;}
    public FeeType getFeeType(){return feeType;} public BigDecimal getAmountDue(){return amountDue;} public String getCurrency(){return currency;}
    public boolean isCountsTowardEligibility(){return countsTowardEligibility;} public FeeObligationStatus getStatus(){return status;}
    public void setStatus(FeeObligationStatus status){this.status=status;} public UserAccount getCreatedBy(){return createdBy;} public OffsetDateTime getCreatedAt(){return createdAt;}
}
