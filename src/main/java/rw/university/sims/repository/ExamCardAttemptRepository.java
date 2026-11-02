package rw.university.sims.repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.university.sims.entity.ExamCardAttempt;
public interface ExamCardAttemptRepository extends JpaRepository<ExamCardAttempt,Long> {
    @EntityGraph(attributePaths={"courseRegistration","examCard","requestedBy"})
    Page<ExamCardAttempt> findByStudent_IdOrderByRequestedAtDesc(Long studentId,Pageable pageable);
}
