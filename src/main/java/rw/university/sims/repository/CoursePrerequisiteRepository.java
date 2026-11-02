package rw.university.sims.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.university.sims.entity.CoursePrerequisite;
import rw.university.sims.entity.CoursePrerequisiteId;

import java.util.List;

public interface CoursePrerequisiteRepository
        extends JpaRepository<CoursePrerequisite, CoursePrerequisiteId> {

    boolean existsByIdCourseIdAndIdPrerequisiteCourseId(
            Long courseId,
            Long prerequisiteCourseId
    );

    @Query("""
            SELECT cp.id.prerequisiteCourseId
            FROM CoursePrerequisite cp
            WHERE cp.id.courseId = :courseId
            """)
    List<Long> findPrerequisiteIdsByCourseId(
            @Param("courseId") Long courseId
    );

    @EntityGraph(attributePaths = "prerequisiteCourse")
    @Query("""
            SELECT cp
            FROM CoursePrerequisite cp
            WHERE cp.id.courseId = :courseId
            ORDER BY cp.prerequisiteCourse.courseCode
            """)
    List<CoursePrerequisite> findAllForCourse(
            @Param("courseId") Long courseId
    );
}