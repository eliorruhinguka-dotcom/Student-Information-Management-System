package rw.university.sims.dto.response;

import java.util.List;

public record RoleResponse(
    Long id,
    String name,
    String description,
    List<String> permissions
) {
}