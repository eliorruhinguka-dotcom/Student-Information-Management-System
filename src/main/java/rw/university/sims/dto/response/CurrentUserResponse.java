package rw.university.sims.dto.response;

import java.util.List;

public record CurrentUserResponse(
    Long id,
    String email,
    String fullName,
    boolean enabled,
    List<String> roles,
    List<String> permissions,
    Long departmentId,
    String departmentName,
    boolean mustChangePassword
) {
}