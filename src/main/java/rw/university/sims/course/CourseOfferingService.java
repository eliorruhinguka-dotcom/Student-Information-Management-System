
package rw.university.sims.course;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CourseOfferingService {

    private final CourseOfferingRepository courseOfferingRepository;

    public CourseOfferingService(
            CourseOfferingRepository courseOfferingRepository) {
        this.courseOfferingRepository = courseOfferingRepository;
    }

    public CourseOffering createCourseOffering(CourseOffering courseOffering) {
        if (courseOfferingRepository.existsByCourseCode(
                courseOffering.getCourseCode())) {
            throw new IllegalArgumentException(
                    "A course offering with this course code already exists");
        }

        return courseOfferingRepository.save(courseOffering);
    }

    @Transactional(readOnly = true)
    public List<CourseOffering> getAllCourseOfferings() {
        return courseOfferingRepository.findAll();
    }

    @Transactional(readOnly = true)
    public CourseOffering getCourseOfferingById(Long id) {
        return courseOfferingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Course offering not found with id: " + id));
    }
}
