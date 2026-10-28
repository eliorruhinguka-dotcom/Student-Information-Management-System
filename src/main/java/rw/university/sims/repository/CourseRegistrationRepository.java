
package rw.university.sims.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.university.sims.entity.CourseRegistration;
import rw.university.sims.enums.RegistrationStatus;

public interface CourseRegistrationRepository
        extends JpaRepository<CourseRegistration, Long> {

    boolean existsByStudent_IdAndCourseOffering_IdAndStatus(
            Long studentId,
            Long courseOfferingId,
            RegistrationStatus status);

    long countByCourseOffering_IdAndStatus(
            Long courseOfferingId,
            RegistrationStatus status);
}
