package rw.university.sims.dto.response;

public record EnrolmentByProgrammeRow(
        Long programmeId,
        String programmeCode,
        String programmeName,
        Long departmentId,
        String departmentCode,
        String departmentName,
        long activeStudentCount,
        long registeredStudentCount,
        long registrationCount) {}
