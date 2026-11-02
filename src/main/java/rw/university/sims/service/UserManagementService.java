package rw.university.sims.service;

import rw.university.sims.dto.request.RoleAssignmentRequest;
import rw.university.sims.dto.request.UserCreateRequest;
import rw.university.sims.dto.response.RoleResponse;
import rw.university.sims.dto.response.PageResponse;
import rw.university.sims.dto.response.UserResponse;
import rw.university.sims.entity.*;
import rw.university.sims.exception.ResourceConflictException;
import rw.university.sims.exception.ResourceNotFoundException;
import rw.university.sims.repository.*;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserManagementService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "id", "email", "fullName", "createdAt", "lastLoginAt", "enabled"
    );

    private final UserAccountRepository userRepository;
    private final RoleRepository roleRepository;
    private final DepartmentRepository departmentRepository;
    private final StudentRepository studentRepository;
    private final StudentUserLinkRepository studentUserLinkRepository;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserManagementService(
            UserAccountRepository userRepository,
            RoleRepository roleRepository,
            DepartmentRepository departmentRepository,
            StudentRepository studentRepository,
            StudentUserLinkRepository studentUserLinkRepository,
            RefreshTokenService refreshTokenService,
            PasswordEncoder passwordEncoder,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.departmentRepository = departmentRepository;
        this.studentRepository = studentRepository;
        this.studentUserLinkRepository = studentUserLinkRepository;
        this.refreshTokenService = refreshTokenService;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional
    public UserResponse createUser(UserCreateRequest request, Long actorId) {
        String email = request.email().trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResourceConflictException(
                "An account with this email already exists"
            );
        }

        validatePassword(request.password());

        Set<Role> roles = resolveRoles(request.roles());
        Set<String> names = roleNames(roles);

        Department department = resolveDepartment(
            request.departmentId(),
            names
        );

        if (names.contains("STUDENT") && request.studentId() == null) {
            throw new IllegalArgumentException(
                "A STUDENT account must be linked to a student record"
            );
        }

        if (!names.contains("STUDENT") && request.studentId() != null) {
            throw new IllegalArgumentException(
                "Only a STUDENT account can be linked to a student record"
            );
        }

        UserAccount actor = userRepository.findById(actorId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Creating user not found")
            );

        UserAccount user = new UserAccount();
        user.setEmail(email);
        user.setFullName(request.fullName().trim());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDepartment(department);
        user.setRoles(roles);
        user.setCreatedBy(actor);
        user.setEnabled(true);

        user = userRepository.save(user);

        if (request.studentId() != null) {
            if (studentUserLinkRepository.existsByStudent_Id(
                    request.studentId())) {
                throw new ResourceConflictException(
                    "This student already has a linked account"
                );
            }

            Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Student not found: " + request.studentId()
                    )
                );

            studentUserLinkRepository.save(
                new StudentUserLink(user, student)
            );
        }

        auditService.record(
            actor, "USER_CREATED", "USER_ACCOUNT",
            user.getId(), "SUCCESS", null, null
        );

        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> searchUsers(
            String role,
            Long departmentId,
            Boolean enabled,
            int page,
            int size,
            String sortBy,
            String direction) {

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                "Page must be zero or greater and size must be between 1 and 100"
            );
        }

        String field = ALLOWED_SORT_FIELDS.contains(sortBy)
            ? sortBy : "createdAt";

        Sort.Direction sortDirection =
            "asc".equalsIgnoreCase(direction)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(
            page,
            size,
            Sort.by(sortDirection, field)
        );

        Specification<UserAccount> specification =
            (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();

                if (role != null && !role.isBlank()) {
                    var roles = root.join("roles", JoinType.INNER);
                    predicates.add(criteriaBuilder.equal(
                        roles.get("name"),
                        role.trim().toUpperCase(Locale.ROOT)
                    ));
                    query.distinct(true);
                }

                if (departmentId != null) {
                    predicates.add(criteriaBuilder.equal(
                        root.get("department").get("id"),
                        departmentId
                    ));
                }

                if (enabled != null) {
                    predicates.add(criteriaBuilder.equal(
                        root.get("enabled"),
                        enabled
                    ));
                }

                return criteriaBuilder.and(
                    predicates.toArray(new Predicate[0])
                );
            };

        Page<UserAccount> result =
            userRepository.findAll(specification, pageable);

        return new PageResponse<>(
            result.getContent().stream()
                .map(this::toResponse)
                .toList(),
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {
        UserAccount user = userRepository.findWithDetailsById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException("User not found: " + id)
            );

        return toResponse(user);
    }

    @Transactional
    public UserResponse replaceRoles(
            Long userId,
            RoleAssignmentRequest request,
            Long actorId) {

        UserAccount user = userRepository.findWithDetailsById(userId)
            .orElseThrow(() ->
                new ResourceNotFoundException("User not found: " + userId)
            );

        Set<Role> roles = resolveRoles(request.roles());
        Set<String> names = roleNames(roles);

        Department department = resolveDepartment(
            request.departmentId(),
            names
        );

        boolean linkedStudent =
            studentUserLinkRepository.existsById(userId);

        if (names.contains("STUDENT") && !linkedStudent) {
            throw new IllegalArgumentException(
                "A STUDENT role requires a linked student record"
            );
        }

        if (!names.contains("STUDENT") && linkedStudent) {
            throw new ResourceConflictException(
                "Remove the student-account link before removing the STUDENT role"
            );
        }

        UserAccount actor = userRepository.findById(actorId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Acting user not found")
            );

        user.setRoles(roles);
        user.setDepartment(department);
        userRepository.save(user);

        auditService.record(
            actor, "USER_ROLES_REPLACED", "USER_ACCOUNT",
            userId, "SUCCESS", null, null
        );

        return toResponse(user);
    }

    @Transactional
    public UserResponse disableUser(Long userId, Long actorId) {
        UserAccount user = userRepository.findWithDetailsById(userId)
            .orElseThrow(() ->
                new ResourceNotFoundException("User not found: " + userId)
            );

        if (!user.isEnabled()) {
            throw new ResourceConflictException(
                "User account is already disabled"
            );
        }

        user.setEnabled(false);
        userRepository.save(user);
        refreshTokenService.revokeAll(userId);

        audit(actorId, "USER_DISABLED", userId);
        return toResponse(user);
    }

    @Transactional
    public UserResponse enableUser(Long userId, Long actorId) {
        UserAccount user = userRepository.findWithDetailsById(userId)
            .orElseThrow(() ->
                new ResourceNotFoundException("User not found: " + userId)
            );

        user.setEnabled(true);
        userRepository.save(user);

        audit(actorId, "USER_ENABLED", userId);
        return toResponse(user);
    }

    @Transactional
    public UserResponse resetPassword(
            Long userId,
            String temporaryPassword,
            Long actorId) {

        validatePassword(temporaryPassword);

        UserAccount user = userRepository.findWithDetailsById(userId)
            .orElseThrow(() ->
                new ResourceNotFoundException("User not found: " + userId)
            );

        user.setPasswordHash(passwordEncoder.encode(temporaryPassword));
        user.setMustChangePassword(true);
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);

        userRepository.save(user);
        refreshTokenService.revokeAll(userId);

        audit(actorId, "PASSWORD_RESET", userId);
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> getRoles() {
        return roleRepository.findAllByOrderByNameAsc()
            .stream()
            .map(role -> new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getPermissions()
                    .stream()
                    .map(Permission::getCode)
                    .sorted()
                    .toList()
            ))
            .toList();
    }

    private Set<Role> resolveRoles(Set<String> requestedNames) {
        Set<Role> roles = new HashSet<>();

        for (String requested : requestedNames) {
            String name = requested.trim().toUpperCase(Locale.ROOT);

            Role role = roleRepository.findByName(name)
                .orElseThrow(() ->
                    new ResourceNotFoundException(
                        "Role not found: " + name
                    )
                );

            roles.add(role);
        }

        return roles;
    }

    private Set<String> roleNames(Set<Role> roles) {
        return roles.stream()
            .map(Role::getName)
            .collect(Collectors.toSet());
    }

    private Department resolveDepartment(
            Long departmentId,
            Set<String> roles) {

        boolean departmentRequired =
            roles.contains("LECTURER") || roles.contains("DEAN");

        if (departmentId == null) {
            if (departmentRequired) {
                throw new IllegalArgumentException(
                    "LECTURER and DEAN accounts require a department"
                );
            }
            return null;
        }

        Department department = departmentRepository.findById(departmentId)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Department not found: " + departmentId
                )
            );

        if (!department.isActive()) {
            throw new IllegalArgumentException(
                "Cannot assign an inactive department"
            );
        }

        return department;
    }

    private void validatePassword(String password) {
        if (password == null
                || password.length() < 12
                || !password.matches(".*[A-Z].*")
                || !password.matches(".*[a-z].*")
                || !password.matches(".*[0-9].*")) {
            throw new IllegalArgumentException(
                "Password must contain at least 12 characters, including uppercase, lowercase and a number"
            );
        }
    }

    private void audit(Long actorId, String action, Long targetId) {
        UserAccount actor = userRepository.findById(actorId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Acting user not found")
            );

        auditService.record(
            actor, action, "USER_ACCOUNT",
            targetId, "SUCCESS", null, null
        );
    }

    private UserResponse toResponse(UserAccount user) {
        return new UserResponse(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.isEnabled(),
            user.getRoles().stream()
                .map(Role::getName)
                .sorted()
                .toList(),
            user.getDepartment() == null
                ? null : user.getDepartment().getId(),
            user.getDepartment() == null
                ? null : user.getDepartment().getName(),
            user.isMustChangePassword(),
            user.getLastLoginAt(),
            user.getCreatedAt()
        );
    }
}