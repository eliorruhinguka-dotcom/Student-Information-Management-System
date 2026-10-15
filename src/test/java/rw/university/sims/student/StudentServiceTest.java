
package rw.university.sims.student;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepository studentRepository;

    private StudentService studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentService(studentRepository);
    }

    @Test
    void shouldCreateStudent() {
        Student student = new Student(
                null, "SIMS001", "Elior", "Ruhinguka",
                "elior@example.com"
        );

        when(studentRepository.save(student)).thenReturn(student);

        Student result = studentService.createStudent(student);

        assertEquals("SIMS001", result.getStudentNumber());
        verify(studentRepository).save(student);
    }

    @Test
    void shouldReturnAllStudents() {
        List<Student> students = List.of(
                new Student(1L, "SIMS001", "Elior", "Ruhinguka",
                        "elior@example.com")
        );

        when(studentRepository.findAll()).thenReturn(students);

        List<Student> result = studentService.getAllStudents();

        assertEquals(1, result.size());
        verify(studentRepository).findAll();
    }

    @Test
    void shouldFindStudentById() {
        Student student = new Student(
                1L, "SIMS001", "Elior", "Ruhinguka",
                "elior@example.com"
        );

        when(studentRepository.findById(1L))
                .thenReturn(Optional.of(student));

        Optional<Student> result = studentService.getStudentById(1L);

        assertTrue(result.isPresent());
        assertEquals("SIMS001", result.get().getStudentNumber());
        verify(studentRepository).findById(1L);
    }

    @Test
    void shouldDeleteStudent() {
        studentService.deleteStudent(1L);

        verify(studentRepository).deleteById(1L);
    }
}
