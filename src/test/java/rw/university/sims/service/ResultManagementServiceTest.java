package rw.university.sims.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
class ResultManagementServiceTest {
    @Mock CourseOfferingRepository offeringRepository;
    @Mock CourseRegistrationRepository registrationRepository;
    @Mock AssessmentRepository assessmentRepository;
    @Mock AssessmentScoreRepository scoreRepository;
    @Mock GradeScaleRepository gradeScaleRepository;
    @Mock AcademicResultRepository resultRepository;
    @Mock ResultCorrectionRepository correctionRepository;
    @Mock UserAccountRepository userRepository;
    @Mock AuditService auditService;
    @InjectMocks ResultManagementService service;

    @Test
    void refusesCalculationUntilAnActiveGradeScaleExists() {
        Department department = new Department("SCI", "Science");
        Course course = new Course("CS101", "Computing", 3, department);
        Semester semester = new Semester("Semester 1", "2099/2100",
                java.time.LocalDate.of(2099, 9, 1), java.time.LocalDate.of(2100, 1, 31), (short) 30);
        Role role = new Role(); role.setName("LECTURER");
        UserAccount lecturer = new UserAccount(); lecturer.setId(9L); lecturer.setRoles(Set.of(role));
        CourseOffering offering = new CourseOffering(); offering.setCourse(course); offering.setSemester(semester);
        offering.setLecturer(lecturer);
        Assessment assessment = new Assessment(offering, "Final", AssessmentType.FINAL_EXAM,
                new BigDecimal("100"), new BigDecimal("100"), lecturer);
        when(offeringRepository.findDetailedById(7L)).thenReturn(Optional.of(offering));
        when(userRepository.findWithDetailsById(9L)).thenReturn(Optional.of(lecturer));
        when(assessmentRepository.findAllByCourseOffering_IdOrderByIdAsc(7L)).thenReturn(List.of(assessment));
        when(gradeScaleRepository.findByActiveTrue()).thenReturn(Optional.empty());
        assertThrows(ResourceConflictException.class, () -> service.calculate(7L, 9L, null));
    }
}
