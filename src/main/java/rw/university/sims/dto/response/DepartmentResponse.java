package rw.university.sims.dto.response;

public record DepartmentResponse(
        Long id,
        String code,
        String name,
        boolean active
) {
}