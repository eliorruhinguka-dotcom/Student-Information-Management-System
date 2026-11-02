package rw.university.sims.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
    name = "semester",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_semester_name_academic_year",
        columnNames = {"name", "academic_year"}
    )
)
public class Semester {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "academic_year", nullable = false, length = 9)
    private String academicYear;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "max_credit_load", nullable = false)
    private Short maxCreditLoad;

    public Semester() {
    }

    public Semester(
            String name,
            String academicYear,
            LocalDate startDate,
            LocalDate endDate,
            Short maxCreditLoad) {
        this.name = name;
        this.academicYear = academicYear;
        this.startDate = startDate;
        this.endDate = endDate;
        this.maxCreditLoad = maxCreditLoad;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAcademicYear() {
        return academicYear;
    }

    public void setAcademicYear(String academicYear) {
        this.academicYear = academicYear;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Short getMaxCreditLoad() {
        return maxCreditLoad;
    }

    public void setMaxCreditLoad(Short maxCreditLoad) {
        this.maxCreditLoad = maxCreditLoad;
    }
}