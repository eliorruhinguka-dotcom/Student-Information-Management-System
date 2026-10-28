
package rw.university.sims.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StudentTest {

    @Test
    void shouldCreateStudentWithCorrectDetails() {
        Student student = new Student(
                1L,
                "SIMS001",
                "Elior",
                "Ruhinguka",
                "elior@example.com"
        );

        assertEquals(1L, student.getId());
        assertEquals("SIMS001", student.getStudentNumber());
        assertEquals("Elior", student.getFirstName());
        assertEquals("Ruhinguka", student.getLastName());
        assertEquals("elior@example.com", student.getEmail());
    }
}
