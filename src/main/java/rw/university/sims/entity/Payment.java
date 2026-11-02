package rw.university.sims.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import rw.university.sims.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "payment")
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "student_id", nullable = false) private Student student;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "semester_id", nullable = false) private Semester semester;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "fee_obligation_id") private FeeObligation feeObligation;
    @Column(nullable = false, precision = 12, scale = 2) private BigDecimal amount;
    @JdbcTypeCode(SqlTypes.CHAR) @Column(nullable = false, length = 3) private String currency = "RWF";
    @Column(name = "transaction_reference", nullable = false, unique = true, length = 100) private String transactionReference;
    @JdbcTypeCode(SqlTypes.CHAR) @Column(name = "request_fingerprint", nullable = false, length = 64) private String requestFingerprint;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private PaymentStatus status = PaymentStatus.PENDING;
    @Column(name = "received_at", nullable = false) private OffsetDateTime receivedAt;
    @Column(name = "confirmed_at") private OffsetDateTime confirmedAt;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "confirmed_by") private UserAccount confirmedBy;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "recorded_by", nullable = false) private UserAccount recordedBy;
    @Version @Column(nullable = false) private Long version = 0L;
    protected Payment() {}
    public Payment(Student student, Semester semester, FeeObligation obligation, BigDecimal amount, String currency,
                   String reference, String fingerprint, OffsetDateTime receivedAt, UserAccount recordedBy) {
        this.student=student; this.semester=semester; this.feeObligation=obligation; this.amount=amount; this.currency=currency;
        this.transactionReference=reference; this.requestFingerprint=fingerprint; this.receivedAt=receivedAt; this.recordedBy=recordedBy;
    }
    public Long getId(){return id;} public Student getStudent(){return student;} public Semester getSemester(){return semester;}
    public FeeObligation getFeeObligation(){return feeObligation;} public BigDecimal getAmount(){return amount;} public String getCurrency(){return currency;}
    public String getTransactionReference(){return transactionReference;} public String getRequestFingerprint(){return requestFingerprint;}
    public PaymentStatus getStatus(){return status;} public void setStatus(PaymentStatus status){this.status=status;}
    public OffsetDateTime getReceivedAt(){return receivedAt;} public OffsetDateTime getConfirmedAt(){return confirmedAt;}
    public void setConfirmedAt(OffsetDateTime value){this.confirmedAt=value;} public UserAccount getConfirmedBy(){return confirmedBy;}
    public void setConfirmedBy(UserAccount value){this.confirmedBy=value;} public UserAccount getRecordedBy(){return recordedBy;} public Long getVersion(){return version;}
}
