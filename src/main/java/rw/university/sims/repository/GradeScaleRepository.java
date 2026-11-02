package rw.university.sims.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.university.sims.entity.GradeScale;
import java.util.Optional;

public interface GradeScaleRepository extends JpaRepository<GradeScale, Long> {
    @EntityGraph(attributePaths = "bands")
    Optional<GradeScale> findByActiveTrue();
}
