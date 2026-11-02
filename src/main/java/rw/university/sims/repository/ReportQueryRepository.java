package rw.university.sims.repository;

import rw.university.sims.dto.response.*;
import java.util.List;

public interface ReportQueryRepository {
    List<EnrolmentByProgrammeRow> enrolmentByProgramme(Long semesterId, Long departmentId, Long lecturerId);
    List<OfferingCapacityRow> offeringCapacity(Long semesterId, Long departmentId, Long lecturerId);
    List<OutstandingFeesStudentRow> outstandingFees(Long semesterId);
    List<GradeDistributionRow> gradeDistribution(Long offeringId);
}
