package rw.university.sims.dto.response;
import java.math.BigDecimal;
public record GradeScaleBandResponse(Long id, BigDecimal minMark, BigDecimal maxMark, String grade, BigDecimal gradePoints, boolean pass) {}
