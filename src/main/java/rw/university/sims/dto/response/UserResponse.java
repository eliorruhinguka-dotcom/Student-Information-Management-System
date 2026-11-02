package rw.university.sims.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record UserResponse(
    Long id,
    String email,
    String fullName,
    boolean enabled,
    List<String> roles,
    Long departmentId,
    String departmentName,
    boolean mustChangePassword,
    OffsetDateTime lastLoginAt,
    OffsetDateTime createdAt
) {
}