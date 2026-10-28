
package rw.university.sims.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    @Test
    void shouldReturnConflictWhenDuplicateIsDetected() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        IllegalArgumentException exception =
                new IllegalArgumentException("Student number already exists");

        ResponseEntity<Map<String, String>> response =
                handler.handleIllegalArgumentException(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(
                "Student number already exists",
                response.getBody().get("error")
        );
    }
}
