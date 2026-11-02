package rw.university.sims.dto.response;

public record ProgrammeResponse(
        Long id,
        String code,
        String name,
        Long departmentId,
        String departmentCode,
        String departmentName,
        Short durationYears,
        boolean active
) {
}