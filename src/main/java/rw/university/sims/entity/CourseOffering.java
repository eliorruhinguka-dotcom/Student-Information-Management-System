package rw.university.sims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

import rw.university.sims.enums.OfferingStatus;

@Entity
@Table(name = "course_offerings")
public class CourseOffering {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Legacy fields retained for compatibility with the earlier API.
    @Column(name = "course_code", length = 20)
    private String courseCode;

    @Column(name = "course_name", length = 100)
    private String courseName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Column(nullable = false, length = 20)
    private String section;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lecturer_id", nullable = false)
    private UserAccount lecturer;

    @Column(nullable = false)
    private Short credits;

    @Column(nullable = false)
    @Min(1)
    private int capacity;

    @Column(name = "registered_count", nullable = false)
    private int registeredCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private OfferingStatus status = OfferingStatus.OPEN;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    public CourseOffering() {
    }

    // Retained for older code and tests.
    public CourseOffering(
            Long id,
            String courseCode,
            String courseName,
            int capacity) {
        this.id = id;
        this.courseCode = courseCode;
        this.courseName = courseName;
        this.capacity = capacity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public UserAccount getLecturer() {
        return lecturer;
    }

    public void setLecturer(UserAccount lecturer) {
        this.lecturer = lecturer;
    }

    public Short getCredits() {
        return credits;
    }

    public void setCredits(Short credits) {
        this.credits = credits;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getRegisteredCount() {
        return registeredCount;
    }

    public void setRegisteredCount(int registeredCount) {
        this.registeredCount = registeredCount;
    }

    public OfferingStatus getStatus() {
        return status;
    }

    public void setStatus(OfferingStatus status) {
        this.status = status;
    }

    public Long getVersion() {
        return version;
    }
}