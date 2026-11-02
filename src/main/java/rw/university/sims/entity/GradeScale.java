package rw.university.sims.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "grade_scale")
public class GradeScale {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 100) private String name;
    @Column(nullable = false) private boolean active;
    @Column(name = "effective_from", nullable = false) private LocalDate effectiveFrom;
    @OneToMany(mappedBy = "gradeScale", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("minMark ASC") private List<GradeScaleBand> bands = new ArrayList<>();
    public GradeScale() {}
    public GradeScale(String name, LocalDate effectiveFrom) { this.name=name; this.effectiveFrom=effectiveFrom; this.active=false; }
    public Long getId(){return id;} public String getName(){return name;} public void setName(String name){this.name=name;}
    public boolean isActive(){return active;} public void setActive(boolean active){this.active=active;}
    public LocalDate getEffectiveFrom(){return effectiveFrom;} public void setEffectiveFrom(LocalDate effectiveFrom){this.effectiveFrom=effectiveFrom;}
    public List<GradeScaleBand> getBands(){return bands;}
    public void replaceBands(List<GradeScaleBand> replacement){bands.clear(); for(GradeScaleBand band:replacement){band.setGradeScale(this); bands.add(band);}}
    public void addBand(GradeScaleBand band){band.setGradeScale(this); bands.add(band);}
}
