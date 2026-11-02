package rw.university.sims.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "result_correction")
public class ResultCorrection {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "result_id", nullable = false)
    private AcademicResult result;
    @Column(name = "original_mark", nullable = false, precision = 5, scale = 2)
    private BigDecimal originalMark;
    @Column(name = "corrected_mark", nullable = false, precision = 5, scale = 2)
    private BigDecimal correctedMark;
    @Column(name = "original_grade", nullable = false, length = 3)
    private String originalGrade;
    @Column(name = "corrected_grade", nullable = false, length = 3)
    private String correctedGrade;
    @Column(nullable = false)
    private String reason;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by")
    private UserAccount requestedBy;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "authorised_by", nullable = false)
    private UserAccount authorisedBy;
    @Column(name = "corrected_at", nullable = false)
    private OffsetDateTime correctedAt;

    protected ResultCorrection() {}
    public ResultCorrection(AcademicResult result, BigDecimal originalMark, BigDecimal correctedMark,
                            String originalGrade, String correctedGrade, String reason,
                            UserAccount requestedBy, UserAccount authorisedBy) {
        this.result = result; this.originalMark = originalMark; this.correctedMark = correctedMark;
        this.originalGrade = originalGrade; this.correctedGrade = correctedGrade; this.reason = reason;
        this.requestedBy = requestedBy; this.authorisedBy = authorisedBy; this.correctedAt = OffsetDateTime.now();
    }
    public Long getId() { return id; }
    public AcademicResult getResult() { return result; }
    public BigDecimal getOriginalMark() { return originalMark; }
    public BigDecimal getCorrectedMark() { return correctedMark; }
    public String getOriginalGrade() { return originalGrade; }
    public String getCorrectedGrade() { return correctedGrade; }
    public String getReason() { return reason; }
    public UserAccount getAuthorisedBy() { return authorisedBy; }
    public OffsetDateTime getCorrectedAt() { return correctedAt; }
}
