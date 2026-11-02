package rw.university.sims.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import rw.university.sims.enums.StudentStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "student")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Keep the Java property name for compatibility; V12 renames the DB column.
    @NotBlank
    @Size(max = 20)
    @Column(name = "registration_number", nullable = false, unique = true, length = 20)
    private String studentNumber;

    @NotBlank @Size(max = 50)
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @NotBlank @Size(max = 50)
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @NotBlank @Email @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String email;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "national_id", length = 30)
    private String nationalId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "programme_id", nullable = true)
    private Programme programme;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudentStatus status = StudentStatus.ACTIVE;

    // Deprecated legacy column retained for older services until the later cleanup migration.
    @Column(name = "semester_fee_paid_percentage", nullable = false)
    private double semesterFeePaidPercentage = 0.0;

    @Column(name = "admitted_at", nullable = false)
    private OffsetDateTime admittedAt;

    @Version
    @Column(nullable = false)
    private Long version = 0L;

    public Student() {}

    public Student(Long id, String studentNumber, String firstName, String lastName, String email) {
        this.id = id;
        this.studentNumber = studentNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    @PrePersist
    void initialiseAdmittedAt() {
        if (admittedAt == null) admittedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }
    public String getRegistrationNumber() { return studentNumber; }
    public void setRegistrationNumber(String registrationNumber) { this.studentNumber = registrationNumber; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getNationalId() { return nationalId; }
    public void setNationalId(String nationalId) { this.nationalId = nationalId; }
    public Programme getProgramme() { return programme; }
    public void setProgramme(Programme programme) { this.programme = programme; }
    public StudentStatus getStatus() { return status; }
    public void setStatus(StudentStatus status) { this.status = status; }
    public double getSemesterFeePaidPercentage() { return semesterFeePaidPercentage; }
    public void setSemesterFeePaidPercentage(double value) { this.semesterFeePaidPercentage = value; }
    public OffsetDateTime getAdmittedAt() { return admittedAt; }
    public void setAdmittedAt(OffsetDateTime admittedAt) { this.admittedAt = admittedAt; }
    public Long getVersion() { return version; }
}
