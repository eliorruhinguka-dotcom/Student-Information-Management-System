package rw.university.sims.registration;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRegistrationRepository
        extends JpaRepository<CourseRegistration, Long> {

    boolean existsByStudent_IdAndCourseOffering_Id(
            Long studentId,
            Long courseOfferingId);

    long countByCourseOffering_Id(Long courseOfferingId);
}