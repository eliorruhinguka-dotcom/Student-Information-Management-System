
package rw.university.sims.course;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/course-offerings")
public class CourseOfferingController {

    private final CourseOfferingService courseOfferingService;

    public CourseOfferingController(
            CourseOfferingService courseOfferingService) {
        this.courseOfferingService = courseOfferingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseOffering createCourseOffering(
            @Valid @RequestBody CourseOffering courseOffering) {
        return courseOfferingService.createCourseOffering(courseOffering);
    }

    @GetMapping
    public List<CourseOffering> getAllCourseOfferings() {
        return courseOfferingService.getAllCourseOfferings();
    }

    @GetMapping("/{id}")
    public CourseOffering getCourseOfferingById(@PathVariable Long id) {
        return courseOfferingService.getCourseOfferingById(id);
    }
}
