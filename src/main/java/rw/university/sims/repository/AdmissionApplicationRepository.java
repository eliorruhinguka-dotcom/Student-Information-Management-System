package rw.university.sims.repository;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import rw.university.sims.entity.AdmissionApplication;
import rw.university.sims.enums.ApplicationStatus;
import java.util.Optional;

public interface AdmissionApplicationRepository extends JpaRepository<AdmissionApplication, Long>,
        JpaSpecificationExecutor<AdmissionApplication> {
    boolean existsByEmailIgnoreCaseAndStatus(String email, ApplicationStatus status);
    @EntityGraph(attributePaths = {"programme", "programme.department", "reviewedBy", "student"})
    @Query("select a from AdmissionApplication a where a.id = :id")
    Optional<AdmissionApplication> findDetailedById(@Param("id") Long id);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from AdmissionApplication a where a.id = :id")
    Optional<AdmissionApplication> findByIdForUpdate(@Param("id") Long id);
    @EntityGraph(attributePaths = {"programme"})
    Page<AdmissionApplication> findAllByStatusAndProgramme_Id(
            ApplicationStatus status, Long programmeId, Pageable pageable);
    @Query(value = "select nextval('admission_application_ref_seq')", nativeQuery = true)
    Long nextReferenceSequence();
}
