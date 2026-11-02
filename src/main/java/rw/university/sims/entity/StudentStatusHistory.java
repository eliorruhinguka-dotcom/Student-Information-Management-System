package rw.university.sims.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "student_status_history")
public class StudentStatusHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    @Column(name = "from_status", nullable = false, length = 20)
    private String fromStatus;
    @Column(name = "to_status", nullable = false, length = 20)
    private String toStatus;
    @Column(nullable = false)
    private String reason;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "changed_by", nullable = false)
    private UserAccount changedBy;
    @Column(name = "changed_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime changedAt;

    protected StudentStatusHistory() {}
    public StudentStatusHistory(Student student, String fromStatus, String toStatus,
                                String reason, UserAccount changedBy) {
        this.student = student;
        this.fromStatus = fromStatus;
        this.toStatus = toStatus;
        this.reason = reason;
        this.changedBy = changedBy;
    }
    public Long getId() { return id; }
    public Student getStudent() { return student; }
    public String getFromStatus() { return fromStatus; }
    public String getToStatus() { return toStatus; }
    public String getReason() { return reason; }
    public UserAccount getChangedBy() { return changedBy; }
    public OffsetDateTime getChangedAt() { return changedAt; }
}
