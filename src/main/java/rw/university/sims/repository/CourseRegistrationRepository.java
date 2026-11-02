package rw.university.sims.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import rw.university.sims.entity.CourseRegistration;
import rw.university.sims.enums.RegistrationStatus;

public interface CourseRegistrationRepository
        extends JpaRepository<CourseRegistration, Long> {

    boolean existsByStudent_IdAndCourseOffering_IdAndStatus(
            Long studentId,
            Long courseOfferingId,
            RegistrationStatus status
    );

    long countByCourseOffering_IdAndStatus(
            Long courseOfferingId,
            RegistrationStatus status
    );

    @EntityGraph(attributePaths = "student")
    Page<CourseRegistration> findByCourseOffering_IdAndStatus(
            Long courseOfferingId,
            RegistrationStatus status,
            Pageable pageable
    );
}