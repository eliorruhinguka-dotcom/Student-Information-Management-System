package rw.university.sims.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.university.sims.entity.Assessment;
import java.util.List;

public interface AssessmentRepository extends JpaRepository<Assessment, Long> {
    List<Assessment> findAllByCourseOffering_IdOrderByIdAsc(Long offeringId);
    boolean existsByCourseOffering_IdAndNameIgnoreCase(Long offeringId, String name);
    boolean existsByCourseOffering_IdAndNameIgnoreCaseAndIdNot(Long offeringId, String name, Long id);
    boolean existsByCourseOffering_Id(Long offeringId);
}
