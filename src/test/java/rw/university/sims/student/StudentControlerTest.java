
package rw.university.sims.student;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StudentControllerTest {

    private StudentService studentService;
    private StudentController studentController;

    @BeforeEach
    void setUp() {
        studentService = mock(StudentService.class);
        studentController = new StudentController(studentService);
    }

    @Test
    void shouldCreateStudent() {
        Student student = new Student(
                null, "SIMS001", "Elior", "Ruhinguka",
                "elior@example.com"
        );

        when(studentService.createStudent(student)).thenReturn(student);

        Student result = studentController.createStudent(student);

        assertEquals("SIMS001", result.getStudentNumber());
        verify(studentService).createStudent(student);
    }

    @Test
    void shouldReturnAllStudents() {
        List<Student> students = List.of(
                new Student(1L, "SIMS001", "Elior", "Ruhinguka",
                        "elior@example.com")
        );

        when(studentService.getAllStudents()).thenReturn(students);

        List<Student> result = studentController.getAllStudents();

        assertEquals(1, result.size());
        verify(studentService).getAllStudents();
    }

    @Test
    void shouldReturnStudentWhenFound() {
        Student student = new Student(
                1L, "SIMS001", "Elior", "Ruhinguka",
                "elior@example.com"
        );

        when(studentService.getStudentById(1L))
                .thenReturn(Optional.of(student));

        ResponseEntity<Student> response =
                studentController.getStudentById(1L);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody() != null);
        assertEquals("SIMS001", response.getBody().getStudentNumber());
    }

    @Test
    void shouldReturnNotFoundWhenStudentDoesNotExist() {
        when(studentService.getStudentById(99L))
                .thenReturn(Optional.empty());

        ResponseEntity<Student> response =
                studentController.getStudentById(99L);

        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void shouldDeleteStudent() {
        ResponseEntity<Void> response =
                studentController.deleteStudent(1L);

        assertEquals(204, response.getStatusCode().value());
        verify(studentService).deleteStudent(1L);
    }
}
