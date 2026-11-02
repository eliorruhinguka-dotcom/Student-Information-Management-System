package rw.university.sims.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;
import rw.university.sims.entity.Student;

import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    boolean existsByStudentNumber(String studentNumber);
    boolean existsByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByNationalId(String nationalId);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
    boolean existsByNationalIdAndIdNot(String nationalId, Long id);

    @EntityGraph(attributePaths = {"programme", "programme.department"})
    @Query("select s from Student s where s.id = :id")
    Optional<Student> findDetailedById(@Param("id") Long id);

    @Query(value = "select nextval('student_reg_seq')", nativeQuery = true)
    Long nextRegistrationSequence();
}
