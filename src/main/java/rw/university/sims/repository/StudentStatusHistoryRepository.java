package rw.university.sims.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.university.sims.entity.StudentStatusHistory;
import java.util.List;

public interface StudentStatusHistoryRepository extends JpaRepository<StudentStatusHistory, Long> {
    @EntityGraph(attributePaths = {"changedBy"})
    List<StudentStatusHistory> findByStudent_IdOrderByChangedAtDesc(Long studentId);
}
