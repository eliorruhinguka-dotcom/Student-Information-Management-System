package rw.university.sims.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.university.sims.entity.ResultCorrection;
import java.util.List;

public interface ResultCorrectionRepository extends JpaRepository<ResultCorrection, Long> {
    @EntityGraph(attributePaths = {"authorisedBy", "requestedBy", "result"})
    List<ResultCorrection> findAllByResult_IdOrderByCorrectedAtDesc(Long resultId);
}
