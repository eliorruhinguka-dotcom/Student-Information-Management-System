package rw.university.sims.entity;

import jakarta.persistence.*;
import rw.university.sims.enums.RegistrationPeriodStatus;

import java.time.OffsetDateTime;

@Entity
@Table(name = "registration_period")
public class RegistrationPeriod {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Column(name = "opens_at", nullable = false)
    private OffsetDateTime opensAt;

    @Column(name = "closes_at", nullable = false)
    private OffsetDateTime closesAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RegistrationPeriodStatus status = RegistrationPeriodStatus.SCHEDULED;

    @Column(name = "opened_by")
    private Long openedBy;

    @Column(name = "closed_by")
    private Long closedBy;

    @Column(name = "closed_at")
    private OffsetDateTime closedAt;

    public RegistrationPeriod() {
    }

    public RegistrationPeriod(
            Semester semester,
            OffsetDateTime opensAt,
            OffsetDateTime closesAt,
            RegistrationPeriodStatus status) {
        this.semester = semester;
        this.opensAt = opensAt;
        this.closesAt = closesAt;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Semester getSemester() {
        return semester;
    }

    public void setSemester(Semester semester) {
        this.semester = semester;
    }

    public OffsetDateTime getOpensAt() {
        return opensAt;
    }

    public void setOpensAt(OffsetDateTime opensAt) {
        this.opensAt = opensAt;
    }

    public OffsetDateTime getClosesAt() {
        return closesAt;
    }

    public void setClosesAt(OffsetDateTime closesAt) {
        this.closesAt = closesAt;
    }

    public RegistrationPeriodStatus getStatus() {
        return status;
    }

    public void setStatus(RegistrationPeriodStatus status) {
        this.status = status;
    }

    public Long getOpenedBy() {
        return openedBy;
    }

    public void setOpenedBy(Long openedBy) {
        this.openedBy = openedBy;
    }

    public Long getClosedBy() {
        return closedBy;
    }

    public void setClosedBy(Long closedBy) {
        this.closedBy = closedBy;
    }

    public OffsetDateTime getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(OffsetDateTime closedAt) {
        this.closedAt = closedAt;
    }
}