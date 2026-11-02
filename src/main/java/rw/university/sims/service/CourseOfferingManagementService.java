package rw.university.sims.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import rw.university.sims.dto.request.OfferingCreateRequest;
import rw.university.sims.dto.request.OfferingStatusRequest;
import rw.university.sims.dto.request.OfferingUpdateRequest;
import rw.university.sims.dto.response.CourseOfferingResponse;
import rw.university.sims.dto.response.OfferingRegistrationResponse;
import rw.university.sims.dto.response.PageResponse;
import rw.university.sims.entity.Course;
import rw.university.sims.entity.CourseOffering;
import rw.university.sims.entity.CourseRegistration;
import rw.university.sims.entity.Department;
import rw.university.sims.entity.Role;
import rw.university.sims.entity.Semester;
import rw.university.sims.entity.UserAccount;
import rw.university.sims.enums.OfferingStatus;
import rw.university.sims.enums.RegistrationStatus;
import rw.university.sims.exception.ResourceConflictException;
import rw.university.sims.exception.ResourceNotFoundException;
import rw.university.sims.repository.CourseOfferingRepository;
import rw.university.sims.repository.CourseRegistrationRepository;
import rw.university.sims.repository.CourseRepository;
import rw.university.sims.repository.SemesterRepository;
import rw.university.sims.repository.UserAccountRepository;

import java.util.Objects;
import java.util.Set;

@Service
@Transactional
public class CourseOfferingManagementService {

    private final CourseOfferingRepository offeringRepository;
    private final CourseRepository courseRepository;
    private final SemesterRepository semesterRepository;
    private final UserAccountRepository userAccountRepository;
    private final CourseRegistrationRepository registrationRepository;
    private final AuditService auditService;

    public CourseOfferingManagementService(
            CourseOfferingRepository offeringRepository,
            CourseRepository courseRepository,
            SemesterRepository semesterRepository,
            UserAccountRepository userAccountRepository,
            CourseRegistrationRepository registrationRepository,
            AuditService auditService) {

        this.offeringRepository = offeringRepository;
        this.courseRepository = courseRepository;
        this.semesterRepository = semesterRepository;
        this.userAccountRepository = userAccountRepository;
        this.registrationRepository = registrationRepository;
        this.auditService = auditService;
    }

    // 1. Create a course offering.
    public CourseOfferingResponse createOffering(
            OfferingCreateRequest request,
            Long actorUserId,
            String ipAddress) {

        Course course = courseRepository.findDetailedById(request.courseId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course not found with ID: " + request.courseId()));

        if (!course.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "An inactive course cannot be offered");
        }

        if (course.getCredits() > Short.MAX_VALUE) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Course credits exceed the offering credit limit");
        }

        Semester semester = semesterRepository.findById(request.semesterId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Semester not found with ID: " + request.semesterId()));

        String section = request.section().trim();

        if (offeringRepository
                .existsByCourse_IdAndSemester_IdAndSectionIgnoreCase(
                        course.getId(), semester.getId(), section)) {
            throw new ResourceConflictException(
                    "This course already has an offering with that section in the semester");
        }

        UserAccount lecturer = validateLecturer(
                request.lecturerId(),
                course.getDepartment()
        );

        UserAccount actor = requireUser(actorUserId);

        CourseOffering offering = new CourseOffering();
        offering.setCourse(course);
        offering.setSemester(semester);
        offering.setSection(section);
        offering.setLecturer(lecturer);
        offering.setCredits((short) course.getCredits());
        offering.setCapacity(request.capacity());
        offering.setRegisteredCount(0);
        offering.setStatus(OfferingStatus.OPEN);

        CourseOffering saved = offeringRepository.saveAndFlush(offering);

        auditService.record(
                actor,
                "COURSE_OFFERING_CREATED",
                "COURSE_OFFERING",
                saved.getId(),
                "SUCCESS",
                ipAddress,
                null
        );

        return toResponse(saved);
    }

    // 2. Search and list offerings with pagination and department scope.
    @Transactional(readOnly = true)
    public PageResponse<CourseOfferingResponse> searchOfferings(
            Long semesterId,
            Long courseId,
            Long departmentId,
            Long lecturerId,
            Pageable pageable,
            Long actorUserId) {

        UserAccount actor = requireUser(actorUserId);
        Set<String> roles = roleNames(actor);

        boolean restrictDepartment = false;
        Long scopeDepartmentId = null;

        if (!hasAnyRole(roles, "REGISTRAR", "STUDENT")) {
            if (hasAnyRole(roles, "LECTURER", "DEAN")) {
                scopeDepartmentId = requireDepartment(actor).getId();
                restrictDepartment = true;
            } else {
                throw forbidden("You cannot view course offerings");
            }
        }

        Page<CourseOffering> page = offeringRepository.searchOfferings(
                semesterId,
                courseId,
                departmentId,
                lecturerId,
                restrictDepartment,
                scopeDepartmentId,
                pageable
        );

        return toOfferingPage(page);
    }

    // 3. Get one offering, checking its department scope.
    @Transactional(readOnly = true)
    public CourseOfferingResponse getOffering(
            Long offeringId,
            Long actorUserId) {

        UserAccount actor = requireUser(actorUserId);

        CourseOffering offering = findDetailedOffering(offeringId);

        assertCanReadOffering(actor, offering);

        return toResponse(offering);
    }

    // 4. Get offerings assigned to the logged-in lecturer.
    @Transactional(readOnly = true)
    public PageResponse<CourseOfferingResponse> getMyOfferings(
            Long actorUserId,
            Pageable pageable) {

        UserAccount actor = requireUser(actorUserId);

        if (!roleNames(actor).contains("LECTURER")) {
            throw forbidden("This endpoint is only for lecturers");
        }

        Page<CourseOffering> page =
                offeringRepository.findAllByLecturer_Id(
                        actorUserId,
                        pageable
                );

        return toOfferingPage(page);
    }

    // 5. Update capacity and assigned lecturer.
    public CourseOfferingResponse updateOffering(
            Long offeringId,
            OfferingUpdateRequest request,
            Long actorUserId,
            String ipAddress) {

        CourseOffering offering = offeringRepository.findByIdForUpdate(
                        offeringId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course offering not found with ID: " + offeringId));

        if (!Objects.equals(request.version(), offering.getVersion())) {
            throw new ResourceConflictException(
                    "Course offering was changed by another user. Refresh and try again.");
        }

        if (request.capacity() < offering.getRegisteredCount()) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Capacity cannot be lower than the current registered count");
        }

        UserAccount lecturer = validateLecturer(
                request.lecturerId(),
                offering.getCourse().getDepartment()
        );

        UserAccount actor = requireUser(actorUserId);

        offering.setCapacity(request.capacity());
        offering.setLecturer(lecturer);

        CourseOffering saved = offeringRepository.saveAndFlush(offering);

        auditService.record(
                actor,
                "COURSE_OFFERING_UPDATED",
                "COURSE_OFFERING",
                saved.getId(),
                "SUCCESS",
                ipAddress,
                null
        );

        return toResponse(saved);
    }

    // 6. Close or cancel an offering.
    public CourseOfferingResponse changeOfferingStatus(
            Long offeringId,
            OfferingStatusRequest request,
            Long actorUserId,
            String ipAddress) {

        CourseOffering offering = offeringRepository.findByIdForUpdate(
                        offeringId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course offering not found with ID: " + offeringId));

        if (offering.getStatus() != OfferingStatus.OPEN) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Only an OPEN offering can be closed or cancelled");
        }

        if (request.status() != OfferingStatus.CLOSED
                && request.status() != OfferingStatus.CANCELLED) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "The new status must be CLOSED or CANCELLED");
        }

        UserAccount actor = requireUser(actorUserId);

        offering.setStatus(request.status());

        CourseOffering saved = offeringRepository.saveAndFlush(offering);

        auditService.record(
                actor,
                request.status() == OfferingStatus.CLOSED
                        ? "COURSE_OFFERING_CLOSED"
                        : "COURSE_OFFERING_CANCELLED",
                "COURSE_OFFERING",
                saved.getId(),
                "SUCCESS",
                ipAddress,
                null
        );

        return toResponse(saved);
    }

    // 7. List registered students in one offering with scope checks.
    @Transactional(readOnly = true)
    public PageResponse<OfferingRegistrationResponse> getOfferingRegistrations(
            Long offeringId,
            Long actorUserId,
            Pageable pageable) {

        UserAccount actor = requireUser(actorUserId);
        CourseOffering offering = findDetailedOffering(offeringId);

        assertCanReadRegistrations(actor, offering);

        Page<CourseRegistration> page =
                registrationRepository.findByCourseOffering_IdAndStatus(
                        offeringId,
                        RegistrationStatus.REGISTERED,
                        pageable
                );

        return new PageResponse<>(
                page.getContent().stream()
                        .map(this::toRegistrationResponse)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private UserAccount validateLecturer(
            Long lecturerId,
            Department courseDepartment) {

        UserAccount lecturer = userAccountRepository.findById(lecturerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Lecturer account not found with ID: " + lecturerId));

        if (!roleNames(lecturer).contains("LECTURER")) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "The selected user does not have the LECTURER role");
        }

        Department lecturerDepartment = lecturer.getDepartment();

        if (lecturerDepartment == null
                || !Objects.equals(
                        lecturerDepartment.getId(),
                        courseDepartment.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "The lecturer must belong to the course department");
        }

        return lecturer;
    }

    private void assertCanReadOffering(
            UserAccount actor,
            CourseOffering offering) {

        Set<String> roles = roleNames(actor);

        if (hasAnyRole(roles, "REGISTRAR", "STUDENT")) {
            return;
        }

        if (hasAnyRole(roles, "LECTURER", "DEAN")) {
            Department department = requireDepartment(actor);

            if (Objects.equals(
                    department.getId(),
                    offering.getCourse().getDepartment().getId())) {
                return;
            }
        }

        throw forbidden("You cannot view this course offering");
    }

    private void assertCanReadRegistrations(
            UserAccount actor,
            CourseOffering offering) {

        Set<String> roles = roleNames(actor);

        if (roles.contains("REGISTRAR")) {
            return;
        }

        if (roles.contains("LECTURER")) {
            if (Objects.equals(
                    actor.getId(),
                    offering.getLecturer().getId())) {
                return;
            }

            throw forbidden(
                    "Lecturers can only view registrations for their own offerings");
        }

        if (roles.contains("DEAN")) {
            Department department = requireDepartment(actor);

            if (Objects.equals(
                    department.getId(),
                    offering.getCourse().getDepartment().getId())) {
                return;
            }
        }

        throw forbidden("You cannot view registrations for this offering");
    }

    private Department requireDepartment(UserAccount user) {
        if (user.getDepartment() == null) {
            throw forbidden("Your account has no assigned department");
        }

        return user.getDepartment();
    }

    private Set<String> roleNames(UserAccount user) {
        return user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(java.util.stream.Collectors.toSet());
    }

    private boolean hasAnyRole(Set<String> roles, String... expected) {
        for (String role : expected) {
            if (roles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    private UserAccount requireUser(Long userId) {
        return userAccountRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Authenticated user account was not found"));
    }

    private CourseOffering findDetailedOffering(Long id) {
        return offeringRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Course offering not found with ID: " + id));
    }

    private ResponseStatusException forbidden(String message) {
        return new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                message
        );
    }

    private PageResponse<CourseOfferingResponse> toOfferingPage(
            Page<CourseOffering> page) {

        return new PageResponse<>(
                page.getContent().stream()
                        .map(this::toResponse)
                        .toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    private CourseOfferingResponse toResponse(CourseOffering offering) {
        Course course = offering.getCourse();
        Semester semester = offering.getSemester();
        UserAccount lecturer = offering.getLecturer();

        return new CourseOfferingResponse(
                offering.getId(),
                course.getId(),
                course.getCourseCode(),
                course.getTitle(),
                semester.getId(),
                semester.getName(),
                semester.getAcademicYear(),
                offering.getSection(),
                lecturer.getId(),
                lecturer.getFullName(),
                offering.getCredits(),
                offering.getCapacity(),
                offering.getRegisteredCount(),
                offering.getCapacity() - offering.getRegisteredCount(),
                offering.getStatus(),
                offering.getVersion()
        );
    }

    private OfferingRegistrationResponse toRegistrationResponse(
            CourseRegistration registration) {

        var student = registration.getStudent();

        return new OfferingRegistrationResponse(
                registration.getId(),
                student.getId(),
                student.getStudentNumber(),
                student.getFirstName(),
                student.getLastName(),
                student.getEmail(),
                registration.getStatus(),
                registration.getRegisteredAt(),
                registration.getCancelledAt()
        );
    }
}