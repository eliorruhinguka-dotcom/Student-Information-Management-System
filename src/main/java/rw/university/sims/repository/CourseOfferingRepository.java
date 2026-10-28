
package rw.university.sims.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.university.sims.entity.CourseOffering;

import java.util.Optional;

public interface CourseOfferingRepository
        extends JpaRepository<CourseOffering, Long> {

    Optional<CourseOffering> findByCourseCode(String courseCode);

    boolean existsByCourseCode(String courseCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CourseOffering c WHERE c.id = :id")
    Optional<CourseOffering> findByIdForUpdate(@Param("id") Long id);
}
