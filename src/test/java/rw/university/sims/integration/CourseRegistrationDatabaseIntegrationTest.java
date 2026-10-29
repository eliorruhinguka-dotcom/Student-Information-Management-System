package rw.university.sims.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import rw.university.sims.entity.CourseOffering;
import rw.university.sims.entity.Student;
import rw.university.sims.enums.RegistrationStatus;
import rw.university.sims.repository.CourseOfferingRepository;
import rw.university.sims.repository.CourseRegistrationRepository;
import rw.university.sims.repository.StudentRepository;
import rw.university.sims.service.CourseRegistrationService;

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

        Student firstStudent = createStudent(token + "A");
        Student secondStudent = createStudent(token + "B");

        firstStudent = studentRepository.saveAndFlush(firstStudent);
        secondStudent = studentRepository.saveAndFlush(secondStudent);

        CourseOffering offering = new CourseOffering(
                null,
                "C" + token,
                "Concurrency Test Course",
                1
        );

        offering = courseOfferingRepository.saveAndFlush(offering);

        // Keep the generated IDs before starting the threads.
        final Long firstStudentId = firstStudent.getId();
        final Long secondStudentId = secondStudent.getId();
        final Long offeringId = offering.getId();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        Callable<Boolean> firstRequest =
                registrationTask(firstStudentId, offeringId, ready, start);

        Callable<Boolean> secondRequest =
                registrationTask(secondStudentId, offeringId, ready, start);

        try {
            Future<Boolean> firstResult = executor.submit(firstRequest);
            Future<Boolean> secondResult = executor.submit(secondRequest);

            assertTrue(
                    ready.await(10, TimeUnit.SECONDS),
                    "Both requests should become ready"
            );

            // Release both requests as close together as possible.
            start.countDown();

            boolean firstSucceeded = firstResult.get(20, TimeUnit.SECONDS);
            boolean secondSucceeded = secondResult.get(20, TimeUnit.SECONDS);

            int successfulRegistrations =
                    (firstSucceeded ? 1 : 0)
                            + (secondSucceeded ? 1 : 0);

            assertEquals(
                    1,
                    successfulRegistrations,
                    "Exactly one student should get the only available place"
            );

            assertEquals(
                    1L,
                    registrationRepository
                            .countByCourseOffering_IdAndStatus(
                                    offeringId,
                                    RegistrationStatus.REGISTERED),
                    "The course must not exceed its capacity"
            );
        } finally {
            start.countDown();
            executor.shutdownNow();
            executor.awaitTermination(10, TimeUnit.SECONDS);

            // Remove the test records in foreign-key-safe order.
            jdbcTemplate.update(
                    "DELETE FROM course_registrations WHERE course_offering_id = ?",
                    offeringId
            );

            jdbcTemplate.update(
                    "DELETE FROM student WHERE id IN (?, ?)",
                    firstStudentId,
                    secondStudentId
            );

            jdbcTemplate.update(
                    "DELETE FROM course_offerings WHERE id = ?",
                    offeringId
            );
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
                // This request was rejected by the registration rules.
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