package rw.university.sims.entity;

import jakarta.persistence.*;
import rw.university.sims.enums.ApplicationStatus;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "admission_application")
public class AdmissionApplication {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 20)
    private String reference;
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;
    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;
    @Column(nullable = false, length = 100)
    private String email;
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;
    @Column(name = "national_id", length = 30)
    private String nationalId;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "programme_id", nullable = false)
    private Programme programme;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private ApplicationStatus status = ApplicationStatus.SUBMITTED;
    @Column(name = "submitted_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime submittedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private UserAccount reviewedBy;
    @Column(name = "reviewed_at")
    private OffsetDateTime reviewedAt;
    @Column(name = "decision_reason")
    private String decisionReason;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", unique = true)
    private Student student;
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    @Version
    @Column(nullable = false)
    private Long version = 0L;

    public Long getId() { return id; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
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
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    public OffsetDateTime getSubmittedAt() { return submittedAt; }
    public UserAccount getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(UserAccount reviewedBy) { this.reviewedBy = reviewedBy; }
    public OffsetDateTime getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(OffsetDateTime reviewedAt) { this.reviewedAt = reviewedAt; }
    public String getDecisionReason() { return decisionReason; }
    public void setDecisionReason(String decisionReason) { this.decisionReason = decisionReason; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    public Long getVersion() { return version; }
}
