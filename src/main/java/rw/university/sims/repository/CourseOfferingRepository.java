package rw.university.sims.repository;

import jakarta.persistence.LockModeType;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rw.university.sims.entity.CourseOffering;

import java.util.Optional;

public interface CourseOfferingRepository
        extends JpaRepository<CourseOffering, Long> {

    // Legacy methods retained for existing registration code.
    Optional<CourseOffering> findByCourseCode(String courseCode);

    boolean existsByCourseCode(String courseCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM CourseOffering o WHERE o.id = :id")
    Optional<CourseOffering> findByIdForUpdate(@Param("id") Long id);

    boolean existsByCourse_IdAndSemester_IdAndSectionIgnoreCase(
            Long courseId,
            Long semesterId,
            String section
    );

    @EntityGraph(attributePaths = {
            "course",
            "course.department",
            "semester",
            "lecturer"
    })
    @Query("SELECT o FROM CourseOffering o WHERE o.id = :id")
    Optional<CourseOffering> findDetailedById(@Param("id") Long id);

    @EntityGraph(attributePaths = {
            "course",
            "course.department",
            "semester",
            "lecturer"
    })
    Page<CourseOffering> findAllByLecturer_Id(
            Long lecturerId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {
            "course",
            "course.department",
            "semester",
            "lecturer"
    })
    @Query("""
            SELECT o
            FROM CourseOffering o
            WHERE (:semesterId IS NULL OR o.semester.id = :semesterId)
              AND (:courseId IS NULL OR o.course.id = :courseId)
              AND (:departmentId IS NULL
                   OR o.course.department.id = :departmentId)
              AND (:lecturerId IS NULL OR o.lecturer.id = :lecturerId)
              AND (:restrictDepartment = false
                   OR o.course.department.id = :scopeDepartmentId)
            """)
    Page<CourseOffering> searchOfferings(
            @Param("semesterId") Long semesterId,
            @Param("courseId") Long courseId,
            @Param("departmentId") Long departmentId,
            @Param("lecturerId") Long lecturerId,
            @Param("restrictDepartment") boolean restrictDepartment,
            @Param("scopeDepartmentId") Long scopeDepartmentId,
            Pageable pageable
    );
}