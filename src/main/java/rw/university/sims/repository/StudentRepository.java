
package rw.university.sims.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import rw.university.sims.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByStudentNumber(String studentNumber);

    boolean existsByEmail(String email);
}
