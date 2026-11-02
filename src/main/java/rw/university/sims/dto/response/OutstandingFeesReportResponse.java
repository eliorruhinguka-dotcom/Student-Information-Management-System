package rw.university.sims.dto.response;

import java.util.List;

public record OutstandingFeesReportResponse(
        Long semesterId,
        List<OutstandingFeesStudentRow> students,
        List<OutstandingFeesCurrencyTotal> totalsByCurrency) {}
