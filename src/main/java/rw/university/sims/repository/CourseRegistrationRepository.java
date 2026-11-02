package rw.university.sims.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.university.sims.entity.CourseRegistration;
import rw.university.sims.enums.RegistrationStatus;
import java.util.List;
import java.util.Optional;

public interface CourseRegistrationRepository extends JpaRepository<CourseRegistration, Long> {
    boolean existsByStudent_IdAndCourseOffering_IdAndStatus(Long studentId, Long courseOfferingId, RegistrationStatus status);
    long countByCourseOffering_IdAndStatus(Long courseOfferingId, RegistrationStatus status);
    @EntityGraph(attributePaths = "student")
    Page<CourseRegistration> findByCourseOffering_IdAndStatus(Long courseOfferingId, RegistrationStatus status, Pageable pageable);
    @EntityGraph(attributePaths = {"student", "courseOffering", "courseOffering.course"})
    List<CourseRegistration> findAllByCourseOffering_IdAndStatus(Long courseOfferingId, RegistrationStatus status);
    boolean existsByStudent_IdAndCourseOffering_Lecturer_IdAndStatus(Long studentId, Long lecturerId, RegistrationStatus status);
    @EntityGraph(attributePaths = {"student", "student.programme", "student.programme.department", "courseOffering",
      "courseOffering.semester", "courseOffering.course", "courseOffering.course.department"})
    @Query("select r from CourseRegistration r where r.id=:id")
    Optional<CourseRegistration> findDetailedById(@Param("id") Long id);
}
