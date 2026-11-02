package rw.university.sims.dto.response;

import java.math.BigDecimal;

public record OfferingCapacityRow(
        Long offeringId,
        String courseCode,
        String courseTitle,
        String departmentCode,
        String departmentName,
        String section,
        String lecturerName,
        int capacity,
        int registeredCount,
        int seatsLeft,
        BigDecimal percentUsed) {}
