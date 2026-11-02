package rw.university.sims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "programme")
public class Programme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Programme code is required")
    @Size(max = 20)
    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @NotBlank(message = "Programme name is required")
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String name;

    @NotNull(message = "Department is required")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @NotNull(message = "Duration is required")
    @Positive(message = "Duration must be greater than zero")
    @Column(name = "duration_years", nullable = false)
    private Short durationYears;

    @Column(nullable = false)
    private boolean active = true;

    public Programme() {
    }

    public Programme(
            String code,
            String name,
            Department department,
            Short durationYears) {
        this.code = code;
        this.name = name;
        this.department = department;
        this.durationYears = durationYears;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Short getDurationYears() {
        return durationYears;
    }

    public void setDurationYears(Short durationYears) {
        this.durationYears = durationYears;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}