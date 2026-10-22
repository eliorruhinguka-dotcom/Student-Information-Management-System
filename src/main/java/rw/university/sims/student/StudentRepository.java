
package rw.university.sims.student;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

    boolean existsByStudentNumber(String studentNumber);

    boolean existsByEmail(String email);
}
