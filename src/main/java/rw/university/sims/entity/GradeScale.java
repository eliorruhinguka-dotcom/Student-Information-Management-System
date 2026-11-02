package rw.university.sims.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "grade_scale")
public class GradeScale {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 100)
    private String name;
    @Column(nullable = false)
    private boolean active;
    @Column(name = "effective_from", nullable = false)
    private LocalDate effectiveFrom;
    @OneToMany(mappedBy = "gradeScale", fetch = FetchType.LAZY)
    private List<GradeScaleBand> bands = new ArrayList<>();
    public Long getId() { return id; }
    public String getName() { return name; }
    public boolean isActive() { return active; }
    public LocalDate getEffectiveFrom() { return effectiveFrom; }
    public List<GradeScaleBand> getBands() { return bands; }
}
