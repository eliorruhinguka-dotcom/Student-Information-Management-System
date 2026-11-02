package rw.university.sims.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.university.sims.dto.request.AssessmentCreateRequest;
import rw.university.sims.entity.*;
import rw.university.sims.enums.AssessmentType;
import rw.university.sims.exception.ResourceConflictException;
import rw.university.sims.repository.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssessmentServiceTest {
    @Mock AssessmentRepository assessmentRepository;
    @Mock AssessmentScoreRepository scoreRepository;
    @Mock CourseOfferingRepository offeringRepository;
    @Mock CourseRegistrationRepository registrationRepository;
    @Mock AcademicResultRepository resultRepository;
    @Mock UserAccountRepository userRepository;
    @Mock AuditService auditService;
    @InjectMocks AssessmentService service;

    @Test
    void rejectsAssessmentWeightsAboveOneHundredPercent() {
        Department department = new Department("SCI", "Science");
        Course course = new Course("CS101", "Computing", 3, department);
        Semester semester = new Semester("Semester 1", "2099/2100",
                java.time.LocalDate.of(2099, 9, 1), java.time.LocalDate.of(2100, 1, 31), (short) 30);
        Role lecturerRole = new Role(); lecturerRole.setName("LECTURER");
        UserAccount lecturer = new UserAccount(); lecturer.setRoles(Set.of(lecturerRole));
        lecturer.setDepartment(department); lecturer.setId(9L);
        CourseOffering offering = new CourseOffering(); offering.setCourse(course); offering.setSemester(semester);
        offering.setLecturer(lecturer);
        Assessment existing = new Assessment(offering, "Midterm", AssessmentType.TEST,
                new BigDecimal("100"), new BigDecimal("90"), lecturer);
        when(offeringRepository.findDetailedById(7L)).thenReturn(Optional.of(offering));
        when(userRepository.findWithDetailsById(9L)).thenReturn(Optional.of(lecturer));
        when(resultRepository.countByCourseRegistration_CourseOffering_IdAndStatus(
                7L, rw.university.sims.enums.ResultStatus.SUBMITTED)).thenReturn(0L);
        when(resultRepository.countByCourseRegistration_CourseOffering_IdAndStatus(
                7L, rw.university.sims.enums.ResultStatus.PUBLISHED)).thenReturn(0L);
        when(assessmentRepository.existsByCourseOffering_IdAndNameIgnoreCase(7L, "Final")) .thenReturn(false);
        when(assessmentRepository.findAllByCourseOffering_IdOrderByIdAsc(7L)).thenReturn(List.of(existing));
        AssessmentCreateRequest request = new AssessmentCreateRequest("Final", AssessmentType.FINAL_EXAM,
                new BigDecimal("100"), new BigDecimal("20"));
        assertThrows(ResourceConflictException.class, () -> service.create(7L, request, 9L, null));
    }
}
