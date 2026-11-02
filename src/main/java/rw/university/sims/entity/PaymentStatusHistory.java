package rw.university.sims.entity;

import jakarta.persistence.*;
import rw.university.sims.enums.PaymentStatus;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payment_status_history")
public class PaymentStatusHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "payment_id", nullable = false) private Payment payment;
    @Enumerated(EnumType.STRING) @Column(name = "from_status", nullable = false, length = 20) private PaymentStatus fromStatus;
    @Enumerated(EnumType.STRING) @Column(name = "to_status", nullable = false, length = 20) private PaymentStatus toStatus;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "changed_by") private UserAccount changedBy;
    @Column(length = 255) private String reason;
    @Column(name = "changed_at", nullable = false, insertable = false, updatable = false) private OffsetDateTime changedAt;
    protected PaymentStatusHistory() {}
    public PaymentStatusHistory(Payment payment, PaymentStatus fromStatus, PaymentStatus toStatus, UserAccount changedBy, String reason){
        this.payment=payment; this.fromStatus=fromStatus; this.toStatus=toStatus; this.changedBy=changedBy; this.reason=reason;
    }
    public Long getId(){return id;} public Payment getPayment(){return payment;} public PaymentStatus getFromStatus(){return fromStatus;}
    public PaymentStatus getToStatus(){return toStatus;} public UserAccount getChangedBy(){return changedBy;} public String getReason(){return reason;} public OffsetDateTime getChangedAt(){return changedAt;}
}
