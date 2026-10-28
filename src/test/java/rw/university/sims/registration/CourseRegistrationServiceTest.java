package rw.university.sims.registration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import rw.university.sims.ResourceNotFoundException;
import rw.university.sims.course.CourseOffering;
import rw.university.sims.course.CourseOfferingRepository;
import rw.university.sims.student.Student;
import rw.university.sims.student.StudentRepository;
import rw.university.sims.student.StudentStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseRegistrationServiceTest {

    private static final Long STUDENT_ID = 1L;
    private static final Long OFFERING_ID = 10L;

    @Mock
    private CourseRegistrationRepository registrationRepository;

    @Mock
    private StudentRepository studentRepository;

    @Mock
    private CourseOfferingRepository courseOfferingRepository;

    @Mock
    private Student student;

    @Mock
    private CourseOffering offering;

    @InjectMocks
    private CourseRegistrationService registrationService;

    @BeforeEach
    void setUp() {
        lenient().when(studentRepository.findById(STUDENT_ID))
                .thenReturn(Optional.of(student));

        lenient().when(student.getStatus())
                .thenReturn(StudentStatus.ACTIVE);

        lenient().when(student.getSemesterFeePaidPercentage())
                .thenReturn(30.0);

        lenient().when(
                        courseOfferingRepository.findByIdForUpdate(OFFERING_ID))
                .thenReturn(Optional.of(offering));

        lenient().when(offering.getCapacity())
                .thenReturn(30);

        lenient().when(
                        registrationRepository
                                .existsByStudent_IdAndCourseOffering_Id(
                                        STUDENT_ID, OFFERING_ID))
                .thenReturn(false);

        lenient().when(
                        registrationRepository.countByCourseOffering_Id(
                                OFFERING_ID))
                .thenReturn(5L);

        lenient().when(
                        registrationRepository.save(any(CourseRegistration.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void shouldRegisterActiveStudentWhoPaidAtLeast30Percent() {
        CourseRegistration result =
                registrationService.registerStudent(
                        STUDENT_ID, OFFERING_ID);

        assertNotNull(result);
        assertSame(student, result.getStudent());
        assertSame(offering, result.getCourseOffering());
        assertNotNull(result.getRegisteredAt());

        verify(registrationRepository)
                .save(any(CourseRegistration.class));
    }

    @Test
    void shouldRejectStudentWhoIsNotActive() {
        when(student.getStatus()).thenReturn(StudentStatus.SUSPENDED);

        assertThrows(
                IllegalArgumentException.class,
                () -> registrationService.registerStudent(
                        STUDENT_ID, OFFERING_ID));

        verify(registrationRepository, never())
                .save(any(CourseRegistration.class));
    }

    @Test
    void shouldRejectStudentWhoPaidLessThan30Percent() {
        when(student.getSemesterFeePaidPercentage())
                .thenReturn(29.0);

        assertThrows(
                IllegalArgumentException.class,
                () -> registrationService.registerStudent(
                        STUDENT_ID, OFFERING_ID));

        verify(registrationRepository, never())
                .save(any(CourseRegistration.class));
    }

    @Test
    void shouldRejectDuplicateRegistration() {
        when(registrationRepository
                .existsByStudent_IdAndCourseOffering_Id(
                        STUDENT_ID, OFFERING_ID))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> registrationService.registerStudent(
                        STUDENT_ID, OFFERING_ID));

        verify(registrationRepository, never())
                .save(any(CourseRegistration.class));
    }

    @Test
    void shouldRejectRegistrationWhenOfferingIsFull() {
        when(registrationRepository.countByCourseOffering_Id(OFFERING_ID))
                .thenReturn(30L);

        assertThrows(
                IllegalArgumentException.class,
                () -> registrationService.registerStudent(
                        STUDENT_ID, OFFERING_ID));

        verify(registrationRepository, never())
                .save(any(CourseRegistration.class));
    }

    @Test
    void shouldRejectRegistrationWhenStudentDoesNotExist() {
        when(studentRepository.findById(STUDENT_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> registrationService.registerStudent(
                        STUDENT_ID, OFFERING_ID));

        verify(registrationRepository, never())
                .save(any(CourseRegistration.class));
    }

    @Test
    void shouldRejectRegistrationWhenOfferingDoesNotExist() {
        when(courseOfferingRepository.findByIdForUpdate(OFFERING_ID))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> registrationService.registerStudent(
                        STUDENT_ID, OFFERING_ID));

        verify(registrationRepository, never())
                .save(any(CourseRegistration.class));
    }
}