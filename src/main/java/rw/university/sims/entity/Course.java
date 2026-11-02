package rw.university.sims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "course")
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Course code is required")
    @Size(max = 20, message = "Course code cannot exceed 20 characters")
    @Column(name = "course_code", nullable = false, unique = true, length = 20)
    private String courseCode;

    @NotBlank(message = "Course title is required")
    @Size(max = 150, message = "Course title cannot exceed 150 characters")
    @Column(nullable = false, length = 150)
    private String title;

    @Min(value = 1, message = "Course credits must be greater than zero")
    @Column(nullable = false)
    private int credits;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Column(nullable = false)
    private boolean active = true;

    public Course() {
    }

    public Course(
            String courseCode,
            String title,
            int credits,
            Department department) {

        this.courseCode = courseCode;
        this.title = title;
        this.credits = credits;
        this.department = department;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}