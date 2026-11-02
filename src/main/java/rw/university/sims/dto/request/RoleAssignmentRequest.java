package rw.university.sims.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;

public record RoleAssignmentRequest(
    @NotEmpty Set<@NotBlank String> roles,
    Long departmentId
) {
}