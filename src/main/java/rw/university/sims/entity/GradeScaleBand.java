package rw.university.sims.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "grade_scale_band")
public class GradeScaleBand {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grade_scale_id", nullable = false)
    private GradeScale gradeScale;
    @Column(name = "min_mark", nullable = false, precision = 5, scale = 2)
    private BigDecimal minMark;
    @Column(name = "max_mark", nullable = false, precision = 5, scale = 2)
    private BigDecimal maxMark;
    @Column(nullable = false, length = 3)
    private String grade;
    @Column(name = "grade_points", nullable = false, precision = 3, scale = 2)
    private BigDecimal gradePoints;
    @Column(name = "is_pass", nullable = false)
    private boolean pass;
    public Long getId() { return id; }
    public GradeScale getGradeScale() { return gradeScale; }
    public BigDecimal getMinMark() { return minMark; }
    public BigDecimal getMaxMark() { return maxMark; }
    public String getGrade() { return grade; }
    public BigDecimal getGradePoints() { return gradePoints; }
    public boolean isPass() { return pass; }
}
