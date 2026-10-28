
package rw.university.sims.entity;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StudentValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    @Test
    void shouldRejectBlankStudentDetails() {
        Student student = new Student(
                null, "", "", "", ""
        );

        assertFalse(validator.validate(student).isEmpty());
    }

    @Test
    void shouldRejectInvalidEmail() {
        Student student = new Student(
                null, "SIMS002", "Alex", "Smith",
                "not-an-email"
        );

        assertFalse(validator.validate(student).isEmpty());
    }

    @Test
    void shouldAcceptValidStudentDetails() {
        Student student = new Student(
                null, "SIMS003", "Alex", "Smith",
                "alex@example.com"
        );

        assertTrue(validator.validate(student).isEmpty());
    }
}
