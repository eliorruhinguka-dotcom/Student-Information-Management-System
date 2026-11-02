package rw.university.sims.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rw.university.sims.entity.Course;

import java.util.Optional;

public interface CourseRepository extends JpaRepository<Course, Long> {

    boolean existsByCourseCodeIgnoreCase(String courseCode);

    @EntityGraph(attributePaths = "department")
    @Query("""
        SELECT c FROM Course c
        WHERE (:departmentId IS NULL OR c.department.id = :departmentId)
        AND (
            :q IS NULL
            OR LOWER(c.courseCode) LIKE LOWER(CONCAT(CONCAT('%', :q), '%'))
            OR LOWER(c.title) LIKE LOWER(CONCAT(CONCAT('%', :q), '%'))
        )
        """)
    Page<Course> searchCourses(
            @Param("departmentId") Long departmentId,
            @Param("q") String searchText,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "department")
    @Query("SELECT c FROM Course c WHERE c.id = :id")
    Optional<Course> findDetailedById(@Param("id") Long id);
}