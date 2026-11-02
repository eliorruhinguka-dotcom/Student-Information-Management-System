package rw.university.sims.dto.response;

import java.math.BigDecimal;

public record GradeDistributionRow(String grade, long count, BigDecimal percentage) {}
