package rw.university.sims.registration;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/registrations")
public class CourseRegistrationController {

    private final CourseRegistrationService registrationService;

    public CourseRegistrationController(
            CourseRegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseRegistrationResponse registerStudent(
            @Valid @RequestBody CourseRegistrationRequest request) {

        CourseRegistration registration =
                registrationService.registerStudent(
                        request.studentId(),
                        request.courseOfferingId());

        return toResponse(registration);
    }

    @GetMapping
    public List<CourseRegistrationResponse> getAllRegistrations() {
        return registrationService.getAllRegistrations()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public CourseRegistrationResponse getRegistrationById(
            @PathVariable Long id) {

        return toResponse(
                registrationService.getRegistrationById(id));
    }

    private CourseRegistrationResponse toResponse(
            CourseRegistration registration) {

        return new CourseRegistrationResponse(
                registration.getId(),
                registration.getStudent().getId(),
                registration.getCourseOffering().getId(),
                registration.getRegisteredAt());
    }
}