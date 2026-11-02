package rw.university.sims.dto.response;

import java.util.List;

public record GradeDistributionReportResponse(
        Long offeringId,
        long totalPublishedResults,
        List<GradeDistributionRow> distribution) {}
