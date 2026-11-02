package rw.university.sims.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import rw.university.sims.dto.response.GenericResponse;

@RestControllerAdvice
public class ResourceConflictExceptionHandler {

    @ExceptionHandler(ResourceConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public GenericResponse<Void> handleConflict(
            ResourceConflictException exception) {

        return GenericResponse.error(
                409,
                "RESOURCE_CONFLICT",
                exception.getMessage()
        );
    }
}