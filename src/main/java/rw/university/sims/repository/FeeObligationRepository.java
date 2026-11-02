package rw.university.sims.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.university.sims.entity.FeeObligation;
import rw.university.sims.enums.FeeObligationStatus;
import java.math.BigDecimal;
import java.util.Optional;
public interface FeeObligationRepository extends JpaRepository<FeeObligation,Long> {
    boolean existsByStudent_IdAndSemester_IdAndFeeType(Long studentId, Long semesterId, rw.university.sims.enums.FeeType feeType);
    boolean existsByStudent_IdAndSemester_IdAndFeeTypeAndIdNot(Long studentId, Long semesterId, rw.university.sims.enums.FeeType feeType, Long id);
    boolean existsByIdAndStudent_Id(Long id, Long studentId);
    @EntityGraph(attributePaths={"student","semester","createdBy"}) Optional<FeeObligation> findDetailedById(Long id);
    @EntityGraph(attributePaths={"student","semester","createdBy"})
    @Query("select f from FeeObligation f where (:studentId is null or f.student.id=:studentId) and (:semesterId is null or f.semester.id=:semesterId) and (:status is null or f.status=:status)")
    Page<FeeObligation> search(@Param("studentId") Long studentId,@Param("semesterId") Long semesterId,
      @Param("status") FeeObligationStatus status,Pageable pageable);
    @Query(value="select coalesce(sum(amount_due),0) from fee_obligation where student_id=:studentId and semester_id=:semesterId and status='ACTIVE' and counts_toward_eligibility=true", nativeQuery=true)
    BigDecimal sumEligibleDue(@Param("studentId") Long studentId,@Param("semesterId") Long semesterId);
}
