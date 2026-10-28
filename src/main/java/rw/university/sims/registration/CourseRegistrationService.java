
package rw.university.sims.registration;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.university.sims.ResourceNotFoundException;
import rw.university.sims.course.CourseOffering;
import rw.university.sims.course.CourseOfferingRepository;
import rw.university.sims.student.Student;
import rw.university.sims.student.StudentRepository;
import rw.university.sims.student.StudentStatus;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CourseRegistrationService {

    private static final double MINIMUM_FEE_PERCENTAGE = 30.0;

    private final CourseRegistrationRepository registrationRepository;
    private final StudentRepository studentRepository;
    private final CourseOfferingRepository courseOfferingRepository;

    public CourseRegistrationService(
            CourseRegistrationRepository registrationRepository,
            StudentRepository studentRepository,
            CourseOfferingRepository courseOfferingRepository) {
        this.registrationRepository = registrationRepository;
        this.studentRepository = studentRepository;
        this.courseOfferingRepository = courseOfferingRepository;
    }

    @Transactional
    public CourseRegistration registerStudent(
            Long studentId,
            Long courseOfferingId) {

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Student not found with id: " + studentId));

        if (student.getStatus() != StudentStatus.ACTIVE) {
            throw new IllegalArgumentException(
                    "Only active students can register for courses");
        }

        if (student.getSemesterFeePaidPercentage()
                < MINIMUM_FEE_PERCENTAGE) {
            throw new IllegalArgumentException(
                    "At least 30% of semester fees must be paid");
        }

        CourseOffering courseOffering =
                courseOfferingRepository.findByIdForUpdate(courseOfferingId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Course offering not found with id: "
                                        + courseOfferingId));

        boolean alreadyRegistered =
                registrationRepository
                        .existsByStudent_IdAndCourseOffering_IdAndStatus(
                                studentId,
                                courseOfferingId,
                                RegistrationStatus.REGISTERED);

        if (alreadyRegistered) {
            throw new IllegalArgumentException(
                    "Student is already registered for this course");
        }

        long currentRegistrations =
                registrationRepository.countByCourseOffering_IdAndStatus(
                        courseOfferingId,
                        RegistrationStatus.REGISTERED);

        if (currentRegistrations >= courseOffering.getCapacity()) {
            throw new IllegalArgumentException(
                    "Course offering has reached its capacity");
        }

        CourseRegistration registration =
                new CourseRegistration(student, courseOffering);

        return registrationRepository.save(registration);
    }

    @Transactional
    public CourseRegistration cancelRegistration(Long registrationId) {
        CourseRegistration registration =
                registrationRepository.findById(registrationId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Registration not found with id: "
                                        + registrationId));

        Long courseOfferingId =
                registration.getCourseOffering().getId();

        // Lock the course offering while cancellation is processed.
        courseOfferingRepository.findByIdForUpdate(courseOfferingId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course offering not found with id: "
                                + courseOfferingId));

        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Registration is already cancelled");
        }

        registration.setStatus(RegistrationStatus.CANCELLED);
        registration.setCancelledAt(LocalDateTime.now());

        return registrationRepository.save(registration);
    }

    @Transactional(readOnly = true)
    public List<CourseRegistration> getAllRegistrations() {
        return registrationRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CourseRegistration getRegistrationById(Long id) {
        return registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Registration not found with id: " + id));
    }
}
