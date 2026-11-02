package rw.university.sims.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import rw.university.sims.entity.ExamCard;
public interface ExamCardRepository extends JpaRepository<ExamCard,Long> {
    @EntityGraph(attributePaths={"courseRegistration","courseRegistration.courseOffering","courseRegistration.courseOffering.course",
      "courseRegistration.courseOffering.course.department","student","semester","requestedBy"})
    Optional<ExamCard> findDetailedByCardNumber(String cardNumber);
    @EntityGraph(attributePaths={"courseRegistration","courseRegistration.courseOffering","courseRegistration.courseOffering.course",
      "student","semester","requestedBy"}) Optional<ExamCard> findByCourseRegistration_Id(Long registrationId);
    @EntityGraph(attributePaths={"courseRegistration","courseRegistration.courseOffering","courseRegistration.courseOffering.course","student","semester"})
    Page<ExamCard> findByStudent_Id(Long studentId,Pageable pageable);
}
