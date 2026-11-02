package rw.university.sims.exception;

import rw.university.sims.dto.response.GenericResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<GenericResponse<Void>> handleResponseStatus(
            ResponseStatusException exception) {

        int status = exception.getStatusCode().value();

        return ResponseEntity.status(status).body(
                GenericResponse.error(
                        status,
                        "REQUEST_FAILED",
                        exception.getReason() == null
                                ? "The request could not be completed"
                                : exception.getReason()
                )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<GenericResponse<Void>> handleValidation(
            MethodArgumentNotValidException exception) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error ->
                        error.getField() + ": " + error.getDefaultMessage()
                )
                .orElse("Request validation failed");

        return ResponseEntity.badRequest().body(
                GenericResponse.error(
                        400,
                        "VALIDATION_ERROR",
                        message
                )
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<GenericResponse<Void>> handleIllegalArgument(
            IllegalArgumentException exception) {

        return ResponseEntity.badRequest().body(
                GenericResponse.error(
                        400,
                        "VALIDATION_ERROR",
                        exception.getMessage()
                )
        );
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GenericResponse<Void>> handleNotFound(
            ResourceNotFoundException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                GenericResponse.error(
                        404,
                        "RESOURCE_NOT_FOUND",
                        exception.getMessage()
                )
        );
    }

    @ExceptionHandler(ResourceConflictException.class)
    public ResponseEntity<GenericResponse<Void>> handleConflict(
            ResourceConflictException exception) {

        return ResponseEntity.status(HttpStatus.CONFLICT).body(
                GenericResponse.error(
                        409,
                        "RESOURCE_CONFLICT",
                        exception.getMessage()
                )
        );
    }
}