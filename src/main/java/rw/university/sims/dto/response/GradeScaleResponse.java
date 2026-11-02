package rw.university.sims.dto.response;
import java.time.LocalDate;
import java.util.List;
public record GradeScaleResponse(Long id, String name, boolean active, LocalDate effectiveFrom, List<GradeScaleBandResponse> bands) {}
