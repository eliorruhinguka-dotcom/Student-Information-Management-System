package rw.university.sims.entity;

import jakarta.persistence.*;
import rw.university.sims.enums.AssessmentType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "assessment", uniqueConstraints = @UniqueConstraint(
        name = "uq_assessment_offering_name", columnNames = {"course_offering_id", "name"}))
public class Assessment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_offering_id", nullable = false)
    private CourseOffering courseOffering;
    @Column(nullable = false, length = 100)
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssessmentType type;
    @Column(name = "max_mark", nullable = false, precision = 5, scale = 2)
    private BigDecimal maxMark;
    @Column(name = "weight_percent", nullable = false, precision = 5, scale = 2)
    private BigDecimal weightPercent;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private UserAccount createdBy;
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    protected Assessment() {}
    public Assessment(CourseOffering offering, String name, AssessmentType type,
                      BigDecimal maxMark, BigDecimal weightPercent, UserAccount actor) {
        this.courseOffering = offering; this.name = name; this.type = type;
        this.maxMark = maxMark; this.weightPercent = weightPercent; this.createdBy = actor; this.createdAt = OffsetDateTime.now();
    }
    public Long getId() { return id; }
    public CourseOffering getCourseOffering() { return courseOffering; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public AssessmentType getType() { return type; }
    public void setType(AssessmentType type) { this.type = type; }
    public BigDecimal getMaxMark() { return maxMark; }
    public void setMaxMark(BigDecimal maxMark) { this.maxMark = maxMark; }
    public BigDecimal getWeightPercent() { return weightPercent; }
    public void setWeightPercent(BigDecimal weightPercent) { this.weightPercent = weightPercent; }
    public UserAccount getCreatedBy() { return createdBy; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
}
