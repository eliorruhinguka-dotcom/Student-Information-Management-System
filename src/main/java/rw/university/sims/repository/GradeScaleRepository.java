package rw.university.sims.repository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.university.sims.entity.GradeScale;
import java.util.List;
import java.util.Optional;
public interface GradeScaleRepository extends JpaRepository<GradeScale, Long> {
    @EntityGraph(attributePaths = "bands") Optional<GradeScale> findByActiveTrue();
    @EntityGraph(attributePaths = "bands") Optional<GradeScale> findDetailedById(Long id);
    @EntityGraph(attributePaths = "bands") List<GradeScale> findAllByOrderByEffectiveFromDesc();
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
