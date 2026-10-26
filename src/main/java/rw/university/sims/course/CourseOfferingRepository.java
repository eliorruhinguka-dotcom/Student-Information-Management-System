
package rw.university.sims.course;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourseOfferingRepository
        extends JpaRepository<CourseOffering, Long> {

    Optional<CourseOffering> findByCourseCode(String courseCode);

    boolean existsByCourseCode(String courseCode);
}
