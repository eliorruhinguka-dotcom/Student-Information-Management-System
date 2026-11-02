package rw.university.sims.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import rw.university.sims.entity.AssessmentScore;
import java.util.List;
import java.util.Optional;

public interface AssessmentScoreRepository extends JpaRepository<AssessmentScore, Long> {
    Optional<AssessmentScore> findByAssessment_IdAndCourseRegistration_Id(Long assessmentId, Long registrationId);
    @EntityGraph(attributePaths = {"courseRegistration", "courseRegistration.student"})
    List<AssessmentScore> findAllByAssessment_IdOrderByCourseRegistration_IdAsc(Long assessmentId);
    boolean existsByAssessment_IdAndMarkIsNull(Long assessmentId);
}
