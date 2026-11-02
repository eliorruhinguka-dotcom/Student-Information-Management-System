package rw.university.sims.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rw.university.sims.dto.request.StudentUpdateRequest;
import rw.university.sims.dto.response.StudentProfileResponse;
import rw.university.sims.dto.response.StudentStatusHistoryResponse;
import rw.university.sims.entity.*;
import rw.university.sims.enums.RegistrationStatus;
import rw.university.sims.exception.ResourceConflictException;
import rw.university.sims.exception.ResourceNotFoundException;
import rw.university.sims.repository.*;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional
public class StudentProfileService {
    private final StudentRepository studentRepository;
    private final ProgrammeRepository programmeRepository;
    private final UserAccountRepository userRepository;
    private final StudentStatusHistoryRepository historyRepository;
    private final CourseRegistrationRepository registrationRepository;
    private final AuditService auditService;

    public StudentProfileService(StudentRepository studentRepository, ProgrammeRepository programmeRepository,
            UserAccountRepository userRepository, StudentStatusHistoryRepository historyRepository,
            CourseRegistrationRepository registrationRepository, AuditService auditService) {
        this.studentRepository = studentRepository; this.programmeRepository = programmeRepository;
        this.userRepository = userRepository; this.historyRepository = historyRepository;
        this.registrationRepository = registrationRepository; this.auditService = auditService;
    }

    public StudentProfileResponse update(Long studentId, StudentUpdateRequest request, Long actorId, String ipAddress) {
        UserAccount actor = requireUser(actorId);
        if (!roleNames(actor).contains("REGISTRAR"))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only a Registrar may update student profiles.");
        Student student = studentRepository.findDetailedById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
        if (!Objects.equals(student.getVersion(), request.version()))
            throw new ResourceConflictException("Student profile changed. Refresh and try again.");
        String email = request.email().trim();
        if (studentRepository.existsByEmailIgnoreCaseAndIdNot(email, studentId))
            throw new ResourceConflictException("Another student already uses this email address.");
        String nationalId = request.nationalId() == null || request.nationalId().isBlank()
                ? null : request.nationalId().trim();
        if (nationalId != null && studentRepository.existsByNationalIdAndIdNot(nationalId, studentId))
            throw new ResourceConflictException("Another student already uses this national ID.");
        Programme programme = programmeRepository.findById(request.programmeId())
                .orElseThrow(() -> new ResourceNotFoundException("Programme not found: " + request.programmeId()));
        if (!programme.isActive()) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                "A student cannot be assigned to an inactive programme");

        student.setFirstName(request.firstName().trim());
        student.setLastName(request.lastName().trim());
        student.setEmail(email);
        student.setDateOfBirth(request.dateOfBirth());
        student.setNationalId(nationalId);
        student.setProgramme(programme);
        Student saved = studentRepository.saveAndFlush(student);
        auditService.record(actor, "STUDENT_PROFILE_UPDATED", "STUDENT", studentId,
                "SUCCESS", ipAddress, null);
        return response(saved);
    }

    @Transactional(readOnly = true)
    public StudentProfileResponse getMyProfile(Long actorId) {
        UserAccount actor = requireUser(actorId);
        if (!roleNames(actor).contains("STUDENT") || actor.getStudentLink() == null
                || actor.getStudentLink().getStudent() == null)
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This account is not linked to a student profile");
        return getProfile(actor.getStudentLink().getStudent().getId());
    }

    @Transactional(readOnly = true)
    public StudentProfileResponse getProfile(Long id) {
        Student student = studentRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
        return response(student);
    }

    @Transactional(readOnly = true)
    public List<StudentStatusHistoryResponse> statusHistory(Long studentId, Long actorId) {
        UserAccount actor = requireUser(actorId);
        Student student = studentRepository.findDetailedById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
        enforceStudentReadScope(actor, student);
        return historyRepository.findByStudent_IdOrderByChangedAtDesc(studentId).stream()
                .map(h -> new StudentStatusHistoryResponse(h.getId(), h.getFromStatus(), h.getToStatus(),
                        h.getReason(), h.getChangedBy().getId(), h.getChangedBy().getFullName(), h.getChangedAt()))
                .toList();
    }

    private void enforceStudentReadScope(UserAccount actor, Student student) {
        Set<String> roles = roleNames(actor);
        if (roles.contains("REGISTRAR") || roles.contains("FINANCE_OFFICER")) return;
        if (roles.contains("STUDENT")) {
            if (actor.getStudentLink() != null && actor.getStudentLink().getStudent() != null
                    && Objects.equals(actor.getStudentLink().getStudent().getId(), student.getId())) return;
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You may only view your own student history");
        }
        if (roles.contains("DEAN")) {
            if (actor.getDepartment() != null && student.getProgramme() != null
                    && student.getProgramme().getDepartment() != null
                    && Objects.equals(actor.getDepartment().getId(), student.getProgramme().getDepartment().getId())) return;
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student is outside your department");
        }
        if (roles.contains("LECTURER") && registrationRepository.existsByStudent_IdAndCourseOffering_Lecturer_IdAndStatus(
                student.getId(), actor.getId(), RegistrationStatus.REGISTERED)) return;
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot view this student's status history");
    }

    private Set<String> roleNames(UserAccount actor) {
        return actor.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet());
    }
    private UserAccount requireUser(Long id) {
        return userRepository.findWithDetailsById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
    private StudentProfileResponse response(Student s) {
        Programme p = s.getProgramme();
        Department d = p == null ? null : p.getDepartment();
        return new StudentProfileResponse(s.getId(), s.getRegistrationNumber(), s.getFirstName(), s.getLastName(),
                s.getEmail(), s.getDateOfBirth(), s.getNationalId(), p == null ? null : p.getId(),
                p == null ? null : p.getCode(), p == null ? null : p.getName(),
                d == null ? null : d.getId(), d == null ? null : d.getName(), s.getStatus(), s.getAdmittedAt(), s.getVersion());
    }
}
