package rw.university.sims.repository;

import rw.university.sims.entity.Semester;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface SemesterRepository extends JpaRepository<Semester, Long> {

    boolean existsByNameIgnoreCaseAndAcademicYear(
        String name,
        String academicYear
    );

    boolean existsByNameIgnoreCaseAndAcademicYearAndIdNot(
        String name,
        String academicYear,
        Long id
    );

    List<Semester> findAllByOrderByStartDateDesc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Semester s WHERE s.id = :id")
    Optional<Semester> findByIdForUpdate(@Param("id") Long id);
}