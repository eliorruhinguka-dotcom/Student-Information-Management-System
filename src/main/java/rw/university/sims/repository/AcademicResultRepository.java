package rw.university.sims.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.university.sims.entity.AcademicResult;
import rw.university.sims.enums.ResultStatus;
import java.util.List;
import java.util.Optional;

public interface AcademicResultRepository extends JpaRepository<AcademicResult, Long> {
    @EntityGraph(attributePaths = {"courseRegistration", "courseRegistration.student", "courseRegistration.courseOffering",
            "courseRegistration.courseOffering.course", "gradeScale"})
    List<AcademicResult> findAllByCourseRegistration_CourseOffering_IdOrderByIdAsc(Long offeringId);
    @EntityGraph(attributePaths = {"courseRegistration", "courseRegistration.student", "courseRegistration.courseOffering",
            "courseRegistration.courseOffering.course", "gradeScale"})
    List<AcademicResult> findAllByCourseRegistration_Student_IdAndStatusOrderByIdDesc(Long studentId, ResultStatus status);
    @EntityGraph(attributePaths = {"courseRegistration", "courseRegistration.student", "courseRegistration.courseOffering",
            "courseRegistration.courseOffering.course", "gradeScale", "gradeScale.bands"})
    @Query("select r from AcademicResult r where r.id = :id")
    Optional<AcademicResult> findDetailedById(@Param("id") Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from AcademicResult r where r.id = :id")
    Optional<AcademicResult> findByIdForUpdate(@Param("id") Long id);
    Optional<AcademicResult> findByCourseRegistration_Id(Long registrationId);
    long countByCourseRegistration_CourseOffering_IdAndStatus(Long offeringId, ResultStatus status);
    List<AcademicResult> findAllByCourseRegistration_CourseOffering_IdAndStatus(Long offeringId, ResultStatus status);
    boolean existsByGradeScale_Id(Long gradeScaleId);
}
