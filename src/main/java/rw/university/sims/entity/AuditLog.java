package rw.university.sims.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Table(name = "audit_log")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "occurred_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime occurredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private UserAccount actor;

    @Column(nullable = false, length = 60)
    private String action;

    @Column(name = "entity_type", length = 60)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_value", columnDefinition = "jsonb")
    private Map<String, Object> oldValue;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_value", columnDefinition = "jsonb")
    private Map<String, Object> newValue;

    @Column(nullable = false, length = 10)
    private String outcome;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "correlation_id", length = 64)
    private String correlationId;

    public AuditLog() {
    }

    public AuditLog(
            UserAccount actor,
            String action,
            String entityType,
            Long entityId,
            String outcome,
            String ipAddress,
            String correlationId) {
        this.actor = actor;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.outcome = outcome;
        this.ipAddress = ipAddress;
        this.correlationId = correlationId;
    }

    public Long getId() { return id; }
    public OffsetDateTime getOccurredAt() { return occurredAt; }
    public UserAccount getActor() { return actor; }
    public String getAction() { return action; }
    public String getEntityType() { return entityType; }
    public Long getEntityId() { return entityId; }
    public Map<String, Object> getOldValue() { return oldValue; }
    public void setOldValue(Map<String, Object> oldValue) { this.oldValue = oldValue; }
    public Map<String, Object> getNewValue() { return newValue; }
    public void setNewValue(Map<String, Object> newValue) { this.newValue = newValue; }
    public String getOutcome() { return outcome; }
    public String getIpAddress() { return ipAddress; }
    public String getCorrelationId() { return correlationId; }
}