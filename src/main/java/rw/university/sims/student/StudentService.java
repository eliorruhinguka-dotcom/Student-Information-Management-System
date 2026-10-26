
package rw.university.sims.student;

import org.springframework.stereotype.Service;
import rw.university.sims.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student createStudent(Student student) {

        if (studentRepository.existsByStudentNumber(
                student.getStudentNumber())) {
            throw new IllegalArgumentException(
                    "Student number already exists");
        }

        if (studentRepository.existsByEmail(student.getEmail())) {
            throw new IllegalArgumentException(
                    "Email address already exists");
        }

        return studentRepository.save(student);
    }

    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    public Student updateStudentStatus(Long id, StudentStatus newStatus) {

        if (newStatus == null) {
            throw new IllegalArgumentException(
                    "Student status is required");
        }

        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with id: " + id));

        student.setStatus(newStatus);

        return studentRepository.save(student);
    }

    public void deleteStudent(Long id) {
        studentRepository.deleteById(id);
    }
}
