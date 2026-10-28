
package rw.university.sims.registration;

import org.springframework.data.jpa.repository.JpaRepository;

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
