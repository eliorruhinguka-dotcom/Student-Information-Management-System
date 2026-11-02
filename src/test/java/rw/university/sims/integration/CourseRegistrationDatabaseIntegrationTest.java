package rw.university.sims.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import rw.university.sims.entity.Course;
import rw.university.sims.entity.CourseOffering;
import rw.university.sims.entity.Department;
import rw.university.sims.entity.Semester;
import rw.university.sims.entity.Student;
import rw.university.sims.entity.UserAccount;
import rw.university.sims.enums.OfferingStatus;
import rw.university.sims.enums.RegistrationStatus;

import rw.university.sims.repository.CourseOfferingRepository;
import rw.university.sims.repository.CourseRegistrationRepository;
import rw.university.sims.repository.CourseRepository;
import rw.university.sims.repository.DepartmentRepository;
import rw.university.sims.repository.SemesterRepository;
import rw.university.sims.repository.StudentRepository;
import rw.university.sims.repository.UserAccountRepository;

import rw.university.sims.service.CourseRegistrationService;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.Callable;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CourseRegistrationConcurrencyIntegrationTest {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private SemesterRepository semesterRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private CourseOfferingRepository courseOfferingRepository;

    @Autowired
    private CourseRegistrationRepository registrationRepository;

    @Autowired
    private CourseRegistrationService registrationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void concurrentRegistrationsMustNotExceedCourseCapacity()
            throws Exception {

        String token = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12);

        Long departmentId = null;
        Long courseId = null;
        Long semesterId = null;
        Long lecturerId = null;
        Long offeringId = null;

        Long firstStudentId = null;
        Long secondStudentId = null;

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            // 1. Create department.
            Department department = new Department(
                    "D" + token,
                    "Concurrency Department " + token
            );

            department = departmentRepository.saveAndFlush(department);
            departmentId = department.getId();

            // 2. Create course.
            Course course = new Course(
                    "C" + token,
                    "Concurrency Test Course",
                    3,
                    department
            );

            course = courseRepository.saveAndFlush(course);
            courseId = course.getId();

            // 3. Create semester.
            Semester semester = new Semester(
                    "Concurrency " + token,
                    "2099-2100",
                    LocalDate.of(2099, 9, 1),
                    LocalDate.of(2100, 6, 30),
                    (short) 30
            );

            semester = semesterRepository.saveAndFlush(semester);
            semesterId = semester.getId();

            // 4. Create lecturer account.
            UserAccount lecturer = new UserAccount();

            lecturer.setEmail("lecturer" + token + "@example.com");
            lecturer.setFullName("Concurrency Test Lecturer");
            lecturer.setPasswordHash("integration-test-password-hash");
            lecturer.setDepartment(department);
            lecturer.setEnabled(true);

            lecturer = userAccountRepository.saveAndFlush(lecturer);
            lecturerId = lecturer.getId();

            // 5. Create two active students.
            Student firstStudent = createStudent(token + "A");
            Student secondStudent = createStudent(token + "B");

            firstStudent = studentRepository.saveAndFlush(firstStudent);
            secondStudent = studentRepository.saveAndFlush(secondStudent);

            firstStudentId = firstStudent.getId();
            secondStudentId = secondStudent.getId();

            // 6. Create offering with capacity of only ONE student.
            CourseOffering offering = new CourseOffering();

            offering.setCourse(course);
            offering.setSemester(semester);
            offering.setSection("A");
            offering.setLecturer(lecturer);
            offering.setCredits((short) course.getCredits());
            offering.setCapacity(1);
            offering.setRegisteredCount(0);
            offering.setStatus(OfferingStatus.OPEN);

            offering = courseOfferingRepository.saveAndFlush(offering);
            offeringId = offering.getId();

            // Keep IDs final for the concurrent tasks.
            final Long firstId = firstStudentId;
            final Long secondId = secondStudentId;
            final Long finalOfferingId = offeringId;

            Callable<Boolean> firstRequest = registrationTask(
                    firstId,
                    finalOfferingId,
                    ready,
                    start
            );

            Callable<Boolean> secondRequest = registrationTask(
                    secondId,
                    finalOfferingId,
                    ready,
                    start
            );

            Future<Boolean> firstResult = executor.submit(firstRequest);
            Future<Boolean> secondResult = executor.submit(secondRequest);

            assertTrue(
                    ready.await(10, TimeUnit.SECONDS),
                    "Both requests should become ready"
            );

            // Start both requests at almost the same time.
            start.countDown();

            boolean firstSucceeded =
                    firstResult.get(20, TimeUnit.SECONDS);

            boolean secondSucceeded =
                    secondResult.get(20, TimeUnit.SECONDS);

            int successfulRegistrations =
                    (firstSucceeded ? 1 : 0)
                            + (secondSucceeded ? 1 : 0);

            // Only one registration should succeed.
            assertEquals(
                    1,
                    successfulRegistrations,
                    "Exactly one student should get the only available place"
            );

            // Check actual registration records.
            assertEquals(
                    1L,
                    registrationRepository
                            .countByCourseOffering_IdAndStatus(
                                    finalOfferingId,
                                    RegistrationStatus.REGISTERED
                            ),
                    "The offering must not exceed its capacity"
            );

            // Check the stored registered_count field too.
            CourseOffering savedOffering =
                    courseOfferingRepository.findById(finalOfferingId)
                            .orElseThrow();

            assertEquals(
                    1,
                    savedOffering.getRegisteredCount(),
                    "The offering registered_count must be exactly one"
            );

        } finally {
            start.countDown();

            executor.shutdownNow();
            executor.awaitTermination(10, TimeUnit.SECONDS);

            // Delete records in foreign-key-safe order.
            if (offeringId != null) {
                jdbcTemplate.update(
                        "DELETE FROM course_registrations WHERE course_offering_id = ?",
                        offeringId
                );

                jdbcTemplate.update(
                        "DELETE FROM course_offerings WHERE id = ?",
                        offeringId
                );
            }

            if (firstStudentId != null && secondStudentId != null) {
                jdbcTemplate.update(
                        "DELETE FROM student WHERE id IN (?, ?)",
                        firstStudentId,
                        secondStudentId
                );
            }

            if (courseId != null) {
                jdbcTemplate.update(
                        "DELETE FROM course WHERE id = ?",
                        courseId
                );
            }

            if (semesterId != null) {
                jdbcTemplate.update(
                        "DELETE FROM semester WHERE id = ?",
                        semesterId
                );
            }

            if (lecturerId != null) {
                jdbcTemplate.update(
                        "DELETE FROM user_accounts WHERE id = ?",
                        lecturerId
                );
            }

            if (departmentId != null) {
                jdbcTemplate.update(
                        "DELETE FROM department WHERE id = ?",
                        departmentId
                );
            }
        }
    }

    private Callable<Boolean> registrationTask(
            Long studentId,
            Long offeringId,
            CountDownLatch ready,
            CountDownLatch start) {

        return () -> {
            ready.countDown();

            if (!start.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException(
                        "Timed out waiting to start registration"
                );
            }

            try {
                registrationService.registerStudent(
                        studentId,
                        offeringId
                );

                return true;

            } catch (IllegalArgumentException exception) {
                return false;
            }
        };
    }

    private Student createStudent(String token) {

        Student student = new Student(
                null,
                "IT" + token,
                "Test",
                "Student",
                "test" + token + "@example.com"
        );

        student.setSemesterFeePaidPercentage(100.0);

        return student;
    }
}