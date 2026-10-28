package rw.university.sims.registration;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.university.sims.ResourceNotFoundException;
import rw.university.sims.course.CourseOffering;
import rw.university.sims.course.CourseOfferingRepository;
import rw.university.sims.student.Student;
import rw.university.sims.student.StudentRepository;
import rw.university.sims.student.StudentStatus;

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

        CourseOffering offering =
                courseOfferingRepository.findByIdForUpdate(courseOfferingId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Course offering not found with id: "
                                        + courseOfferingId));

        if (registrationRepository
                .existsByStudent_IdAndCourseOffering_Id(
                        studentId, courseOfferingId)) {
            throw new IllegalArgumentException(
                    "Student is already registered for this course");
        }

        long currentRegistrations =
                registrationRepository.countByCourseOffering_Id(
                        courseOfferingId);

        if (currentRegistrations >= offering.getCapacity()) {
            throw new IllegalArgumentException(
                    "Course offering has reached its capacity");
        }

        CourseRegistration registration =
                new CourseRegistration(student, offering);

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