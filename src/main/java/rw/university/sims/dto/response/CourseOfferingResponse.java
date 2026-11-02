package rw.university.sims.dto.response;

import rw.university.sims.enums.OfferingStatus;

public record CourseOfferingResponse(
        Long id,
        Long courseId,
        String courseCode,
        String courseTitle,
        Long semesterId,
        String semesterName,
        String academicYear,
        String section,
        Long lecturerId,
        String lecturerName,
        Short credits,
        int capacity,
        int registeredCount,
        int seatsLeft,
        OfferingStatus status,
        Long version
) {
}