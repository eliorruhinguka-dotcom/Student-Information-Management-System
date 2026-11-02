package rw.university.sims.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record UserCreateRequest(
    @NotBlank @Size(max = 150) String fullName,
    @NotBlank @Email @Size(max = 150) String email,
    @NotBlank @Size(min = 12, max = 100) String password,
    Long departmentId,
    @NotEmpty Set<@NotBlank String> roles,
    Long studentId
) {
}