package rw.university.sims.repository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.university.sims.entity.PaymentStatusHistory;
import java.util.List;
public interface PaymentStatusHistoryRepository extends JpaRepository<PaymentStatusHistory,Long> {
    @EntityGraph(attributePaths="changedBy") List<PaymentStatusHistory> findByPayment_IdOrderByChangedAtDesc(Long paymentId);
}
