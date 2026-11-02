package rw.university.sims.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.university.sims.entity.Payment;
import rw.university.sims.enums.PaymentStatus;
import java.math.BigDecimal;
import java.util.Optional;
public interface PaymentRepository extends JpaRepository<Payment,Long> {
    Optional<Payment> findByTransactionReference(String reference);
    boolean existsByFeeObligation_Id(Long feeObligationId);
    @EntityGraph(attributePaths={"student","semester","feeObligation","confirmedBy","recordedBy"}) Optional<Payment> findDetailedById(Long id);
    @EntityGraph(attributePaths={"student","semester","feeObligation","confirmedBy","recordedBy"})
    @Query("select p from Payment p where (:studentId is null or p.student.id=:studentId) and (:semesterId is null or p.semester.id=:semesterId) and (:status is null or p.status=:status)")
    Page<Payment> search(@Param("studentId") Long studentId,@Param("semesterId") Long semesterId,
      @Param("status") PaymentStatus status,Pageable pageable);
    @EntityGraph(attributePaths={"student","semester","feeObligation","confirmedBy","recordedBy"})
    Page<Payment> findByStudent_Id(Long studentId,Pageable pageable);
    @Query(value="select coalesce(sum(amount),0) from payment where student_id=:studentId and semester_id=:semesterId and status='CONFIRMED'", nativeQuery=true)
    BigDecimal sumConfirmed(@Param("studentId") Long studentId,@Param("semesterId") Long semesterId);
}
