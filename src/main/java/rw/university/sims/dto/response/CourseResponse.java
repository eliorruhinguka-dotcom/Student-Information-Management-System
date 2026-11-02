package rw.university.sims.dto.response;

public record CourseResponse(
        Long id,
        String courseCode,
        String title,
        int credits,
        Long departmentId,
        String departmentCode,
        String departmentName,
        boolean active
) {
}