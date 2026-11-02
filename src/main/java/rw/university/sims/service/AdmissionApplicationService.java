package rw.university.sims.service;

import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import rw.university.sims.dto.request.*;
import rw.university.sims.dto.response.*;
import rw.university.sims.entity.*;
import rw.university.sims.enums.*;
import rw.university.sims.exception.ResourceConflictException;
import rw.university.sims.exception.ResourceNotFoundException;
import rw.university.sims.repository.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;

@Service
public class AdmissionApplicationService {
    private final AdmissionApplicationRepository applicationRepository;
    private final ProgrammeRepository programmeRepository;
    private final StudentRepository studentRepository;
    private final StudentStatusHistoryRepository historyRepository;
    private final UserAccountRepository userRepository;
    private final AuditService auditService;
    private final AdmissionRateLimiter rateLimiter;

    public AdmissionApplicationService(AdmissionApplicationRepository applicationRepository,
            ProgrammeRepository programmeRepository, StudentRepository studentRepository,
            StudentStatusHistoryRepository historyRepository, UserAccountRepository userRepository,
            AuditService auditService, AdmissionRateLimiter rateLimiter) {
        this.applicationRepository = applicationRepository;
        this.programmeRepository = programmeRepository;
        this.studentRepository = studentRepository;
        this.historyRepository = historyRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
        this.rateLimiter = rateLimiter;
    }

    @Transactional
    public ApplicationReceiptResponse submit(AdmissionApplicationCreateRequest request, String ipAddress) {
        rateLimiter.check(ipAddress);
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (studentRepository.existsByEmailIgnoreCase(email)
                || (request.nationalId() != null && !request.nationalId().isBlank()
                    && studentRepository.existsByNationalId(request.nationalId().trim()))
                || applicationRepository.existsByEmailIgnoreCaseAndStatus(email, ApplicationStatus.SUBMITTED)) {
            // Same message/status for existing students and open applications to reduce account enumeration.
            throw new ResourceConflictException("An application using these details cannot be accepted.");
        }
        Programme programme = programmeRepository.findById(request.programmeId())
                .orElseThrow(() -> new ResourceNotFoundException("Programme not found: " + request.programmeId()));
        if (!programme.isActive()) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                "Applications cannot be submitted to an inactive programme");

        long sequence = applicationRepository.nextReferenceSequence();
        AdmissionApplication application = new AdmissionApplication();
        application.setReference(String.format(Locale.ROOT, "APP-%d-%06d", LocalDate.now().getYear(), sequence));
        application.setFirstName(request.firstName().trim());
        application.setLastName(request.lastName().trim());
        application.setEmail(email);
        application.setDateOfBirth(request.dateOfBirth());
        application.setNationalId(blankToNull(request.nationalId()));
        application.setProgramme(programme);
        application.setStatus(ApplicationStatus.SUBMITTED);
        application.setIpAddress(ipAddress);
        AdmissionApplication saved = applicationRepository.saveAndFlush(application);
        auditService.record(null, "APPLICATION_SUBMITTED", "ADMISSION_APPLICATION",
                saved.getId(), "SUCCESS", ipAddress, null);
        return new ApplicationReceiptResponse(saved.getReference(), "Your application was received. Keep this reference for future enquiries.");
    }

    @Transactional(readOnly = true)
    public PageResponse<AdmissionApplicationResponse> search(ApplicationStatus status, Long programmeId,
            String query, int page, int size, String sortBy, String direction) {
        Pageable pageable = pageable(page, size, sortBy, direction);
        Specification<AdmissionApplication> specification = (root, cq, cb) -> {
            ArrayList<Predicate> predicates = new ArrayList<>();
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (programmeId != null) predicates.add(cb.equal(root.get("programme").get("id"), programmeId));
            if (query != null && !query.isBlank()) {
                String pattern = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(cb.like(cb.lower(root.get("firstName")), pattern),
                        cb.like(cb.lower(root.get("lastName")), pattern),
                        cb.like(cb.lower(root.get("email")), pattern),
                        cb.like(cb.lower(root.get("reference")), pattern)));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        Page<AdmissionApplication> result = applicationRepository.findAll(specification, pageable);
        return new PageResponse<>(result.getContent().stream().map(this::response).toList(),
                result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
    }

    @Transactional(readOnly = true)
    public AdmissionApplicationResponse get(Long id) { return response(findDetailed(id)); }

    @Transactional
    public StudentProfileResponse approve(Long id, AdmissionApprovalRequest request, Long actorId, String ipAddress) {
        AdmissionApplication application = applicationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));
        checkDecision(application, request.version());
        if (studentRepository.existsByEmailIgnoreCase(application.getEmail())
                || (application.getNationalId() != null && studentRepository.existsByNationalId(application.getNationalId()))) {
            throw new ResourceConflictException("The applicant's email or national ID is already assigned to a student.");
        }
        UserAccount actor = requireUser(actorId);
        if (!roleNames(actor).contains("REGISTRAR"))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only a Registrar may approve applications.");
        Student student = new Student();
        student.setStudentNumber(String.format(Locale.ROOT, "UGL-%d-%06d",
                LocalDate.now().getYear(), studentRepository.nextRegistrationSequence()));
        student.setFirstName(application.getFirstName());
        student.setLastName(application.getLastName());
        student.setEmail(application.getEmail());
        student.setDateOfBirth(application.getDateOfBirth());
        student.setNationalId(application.getNationalId());
        student.setProgramme(application.getProgramme());
        student.setStatus(StudentStatus.ACTIVE);
        student.setAdmittedAt(OffsetDateTime.now());
        student = studentRepository.saveAndFlush(student);

        application.setStatus(ApplicationStatus.APPROVED);
        application.setReviewedBy(actor);
        application.setReviewedAt(OffsetDateTime.now());
        application.setStudent(student);
        application.setDecisionReason(null);
        applicationRepository.saveAndFlush(application);
        historyRepository.save(new StudentStatusHistory(student, "APPLICANT", "ACTIVE",
                "Admission application approved", actor));
        auditService.record(actor, "APPLICATION_APPROVED", "ADMISSION_APPLICATION",
                id, "SUCCESS", ipAddress, null);
        return studentResponse(student);
    }

    @Transactional
    public AdmissionApplicationResponse reject(Long id, AdmissionRejectRequest request, Long actorId, String ipAddress) {
        AdmissionApplication application = applicationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));
        checkDecision(application, request.version());
        UserAccount actor = requireUser(actorId);
        if (!roleNames(actor).contains("REGISTRAR"))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only a Registrar may reject applications.");
        application.setStatus(ApplicationStatus.REJECTED);
        application.setReviewedBy(actor);
        application.setReviewedAt(OffsetDateTime.now());
        application.setDecisionReason(request.reason().trim());
        AdmissionApplication saved = applicationRepository.saveAndFlush(application);
        auditService.record(actor, "APPLICATION_REJECTED", "ADMISSION_APPLICATION",
                id, "SUCCESS", ipAddress, null);
        return response(saved);
    }

    private void checkDecision(AdmissionApplication application, Long version) {
        if (application.getStatus() != ApplicationStatus.SUBMITTED)
            throw new ResourceConflictException("Only SUBMITTED applications can be decided.");
        if (!Objects.equals(application.getVersion(), version))
            throw new ResourceConflictException("Application changed. Refresh and try again.");
    }
    private AdmissionApplication findDetailed(Long id) {
        return applicationRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));
    }
    private java.util.Set<String> roleNames(UserAccount actor) {
        return actor.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet());
    }
    private UserAccount requireUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }
    private String blankToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
    private Pageable pageable(int page, int size, String sortBy, String direction) {
        if (page < 0 || size < 1 || size > 100) throw new IllegalArgumentException("Invalid page or size");
        if (!java.util.Set.of("id", "reference", "firstName", "lastName", "email", "status", "submittedAt").contains(sortBy))
            throw new IllegalArgumentException("Unsupported sort field: " + sortBy);
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(dir, sortBy));
    }
    private AdmissionApplicationResponse response(AdmissionApplication a) {
        return new AdmissionApplicationResponse(a.getId(), a.getReference(), a.getFirstName(), a.getLastName(),
                a.getEmail(), a.getDateOfBirth(), a.getNationalId(), a.getProgramme().getId(),
                a.getProgramme().getName(), a.getStatus(), a.getSubmittedAt(), a.getReviewedAt(),
                a.getDecisionReason(), a.getStudent() == null ? null : a.getStudent().getId(), a.getVersion());
    }
    private StudentProfileResponse studentResponse(Student s) {
        Programme p = s.getProgramme();
        return new StudentProfileResponse(s.getId(), s.getRegistrationNumber(), s.getFirstName(), s.getLastName(),
                s.getEmail(), s.getDateOfBirth(), s.getNationalId(), p.getId(), p.getCode(), p.getName(),
                p.getDepartment().getId(), p.getDepartment().getName(), s.getStatus(), s.getAdmittedAt(), s.getVersion());
    }
}
