package rw.university.sims.exception;

import rw.university.sims.dto.response.GenericResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GlobalExceptionHandlerTest {

    @Test
    void shouldReturnBadRequestWhenIllegalArgumentIsDetected() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        IllegalArgumentException exception =
                new IllegalArgumentException("Student number already exists");

        ResponseEntity<GenericResponse<Void>> response =
                handler.handleIllegalArgument(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().status());
        assertEquals("VALIDATION_ERROR", response.getBody().code());
        assertEquals(
                "Student number already exists",
                response.getBody().message()
        );
        assertNull(response.getBody().data());
    }
}
