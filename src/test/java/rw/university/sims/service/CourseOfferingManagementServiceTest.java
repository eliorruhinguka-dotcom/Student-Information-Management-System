package rw.university.sims.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import rw.university.sims.dto.request.OfferingCreateRequest;
import rw.university.sims.entity.Course;
import rw.university.sims.entity.CourseOffering;
import rw.university.sims.entity.Department;
import rw.university.sims.entity.Role;
import rw.university.sims.entity.Semester;
import rw.university.sims.entity.UserAccount;
import rw.university.sims.exception.ResourceConflictException;
import rw.university.sims.repository.CourseOfferingRepository;
import rw.university.sims.repository.CourseRegistrationRepository;
import rw.university.sims.repository.CourseRepository;
import rw.university.sims.repository.SemesterRepository;
import rw.university.sims.repository.UserAccountRepository;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseOfferingManagementServiceTest {

    @Mock
    private CourseOfferingRepository offeringRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private SemesterRepository semesterRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private CourseRegistrationRepository registrationRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private CourseOfferingManagementService offeringService;

    private Department department(Long id) {
        Department department = mock(Department.class);

        lenient().when(department.getId()).thenReturn(id);

        return department;
    }

    private Role lecturerRole() {
        Role role = new Role();
        role.setName("LECTURER");
        return role;
    }

    private void prepareCreateScenario() {

        Department courseDepartment = department(10L);

        Course course = mock(Course.class);

        lenient().when(course.getId()).thenReturn(1L);
        lenient().when(course.getCourseCode()).thenReturn("CS101");
        lenient().when(course.getTitle())
                .thenReturn("Introduction to Computing");
        lenient().when(course.getCredits()).thenReturn(3);
        lenient().when(course.getDepartment())
                .thenReturn(courseDepartment);
        lenient().when(course.isActive()).thenReturn(true);

        Semester semester = mock(Semester.class);

        lenient().when(semester.getId()).thenReturn(2L);
        lenient().when(semester.getName()).thenReturn("Semester One");
        lenient().when(semester.getAcademicYear())
                .thenReturn("2026-2027");

        UserAccount lecturer = mock(UserAccount.class);

        lenient().when(lecturer.getId()).thenReturn(8L);
        lenient().when(lecturer.getFullName()).thenReturn("Lecturer One");
        lenient().when(lecturer.getDepartment())
                .thenReturn(courseDepartment);
        lenient().when(lecturer.getRoles())
                .thenReturn(Set.of(lecturerRole()));

        UserAccount actor = mock(UserAccount.class);

        lenient().when(courseRepository.findDetailedById(1L))
                .thenReturn(Optional.of(course));

        lenient().when(semesterRepository.findById(2L))
                .thenReturn(Optional.of(semester));

        lenient().when(userAccountRepository.findById(8L))
                .thenReturn(Optional.of(lecturer));

        lenient().when(userAccountRepository.findById(9L))
                .thenReturn(Optional.of(actor));

        lenient().when(
                offeringRepository
                    .existsByCourse_IdAndSemester_IdAndSectionIgnoreCase(
                            1L, 2L, "A")
        ).thenReturn(false);

        lenient().when(
                offeringRepository.saveAndFlush(any(CourseOffering.class))
        ).thenAnswer(invocation -> {
            CourseOffering offering = invocation.getArgument(0);
            offering.setId(100L);
            return offering;
        });
    }

    @Test
    void shouldCreateOfferingAndCopyCourseCredits() {

        prepareCreateScenario();

        var response = offeringService.createOffering(
                new OfferingCreateRequest(1L, 2L, " A ", 8L, 30),
                9L,
                "127.0.0.1"
        );

        assertEquals(100L, response.id());
        assertEquals(1L, response.courseId());
        assertEquals("CS101", response.courseCode());
        assertEquals(2L, response.semesterId());
        assertEquals("A", response.section());
        assertEquals(8L, response.lecturerId());
        assertEquals(Short.valueOf((short) 3), response.credits());
        assertEquals(30, response.capacity());
        assertEquals(0, response.registeredCount());
        assertEquals(30, response.seatsLeft());
        assertEquals("OPEN", response.status().name());

        verify(offeringRepository)
                .saveAndFlush(any(CourseOffering.class));

        verify(auditService).record(
                any(UserAccount.class),
                eq("COURSE_OFFERING_CREATED"),
                eq("COURSE_OFFERING"),
                eq(100L),
                eq("SUCCESS"),
                eq("127.0.0.1"),
                isNull()
        );
    }

    @Test
    void shouldRejectDuplicateCourseSemesterAndSection() {

        prepareCreateScenario();

        when(
                offeringRepository
                    .existsByCourse_IdAndSemester_IdAndSectionIgnoreCase(
                            1L, 2L, "A")
        ).thenReturn(true);

        assertThrows(
                ResourceConflictException.class,
                () -> offeringService.createOffering(
                        new OfferingCreateRequest(1L, 2L, "A", 8L, 30),
                        9L,
                        "127.0.0.1"
                )
        );

        verify(offeringRepository, never())
                .saveAndFlush(any(CourseOffering.class));
    }

    @Test
    void shouldRejectLecturerFromDifferentDepartment() {

        prepareCreateScenario();

        UserAccount lecturer =
                userAccountRepository.findById(8L).orElseThrow();

        Department otherDepartment = department(20L);

        when(lecturer.getDepartment())
                .thenReturn(otherDepartment);

        assertThrows(
                org.springframework.web.server.ResponseStatusException.class,
                () -> offeringService.createOffering(
                        new OfferingCreateRequest(1L, 2L, "A", 8L, 30),
                        9L,
                        "127.0.0.1"
                )
        );

        verify(offeringRepository, never())
                .saveAndFlush(any(CourseOffering.class));
    }
}
