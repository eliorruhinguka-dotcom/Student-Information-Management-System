package rw.university.sims.service;

import rw.university.sims.dto.request.ChangePasswordRequest;
import rw.university.sims.dto.response.AuthResponse;
import rw.university.sims.dto.response.CurrentUserResponse;
import rw.university.sims.entity.Permission;
import rw.university.sims.entity.Role;
import rw.university.sims.entity.UserAccount;
import rw.university.sims.repository.UserAccountRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class AuthenticationService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final AuditService auditService;
    private final long accessTokenMinutes;
    private final int maxFailedAttempts;
    private final long lockoutMinutes;

    public AuthenticationService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            RefreshTokenService refreshTokenService,
            AuditService auditService,
            @Value("${security.jwt.access-token-minutes:15}")
            long accessTokenMinutes,
            @Value("${security.login.max-failed-attempts:5}")
            int maxFailedAttempts,
            @Value("${security.login.lockout-minutes:15}")
            long lockoutMinutes) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.auditService = auditService;
        this.accessTokenMinutes = accessTokenMinutes;
        this.maxFailedAttempts = maxFailedAttempts;
        this.lockoutMinutes = lockoutMinutes;
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public AuthResponse login(String email, String password, String ipAddress) {
        UserAccount user = userAccountRepository
            .findWithRolesByEmailIgnoreCase(email.trim())
            .orElse(null);

        if (user == null) {
            auditService.record(
                null, "LOGIN_FAILED", "USER_ACCOUNT",
                null, "DENIED", ipAddress, null
            );
            throw unauthorized();
        }

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        if (!user.isEnabled()) {
            auditService.record(
                user, "LOGIN_FAILED", "USER_ACCOUNT",
                user.getId(), "DENIED", ipAddress, null
            );
            throw new ResponseStatusException(
                HttpStatus.LOCKED,
                "Account is disabled"
            );
        }

        if (user.getLockedUntil() != null
                && user.getLockedUntil().isAfter(now)) {
            throw new ResponseStatusException(
                HttpStatus.LOCKED,
                "Account is temporarily locked"
            );
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            int failures = user.getFailedLoginCount() + 1;
            user.setFailedLoginCount(failures);

            if (failures >= maxFailedAttempts) {
                user.setLockedUntil(now.plusMinutes(lockoutMinutes));
            }

            userAccountRepository.save(user);

            auditService.record(
                user, "LOGIN_FAILED", "USER_ACCOUNT",
                user.getId(), "DENIED", ipAddress, null
            );

            if (failures >= maxFailedAttempts) {
                throw new ResponseStatusException(
                    HttpStatus.LOCKED,
                    "Too many failed attempts. Account temporarily locked."
                );
            }

            throw unauthorized();
        }

        user.setFailedLoginCount(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(now);
        userAccountRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = refreshTokenService.create(user);

        auditService.record(
            user, "LOGIN_SUCCESS", "USER_ACCOUNT",
            user.getId(), "SUCCESS", ipAddress, null
        );

        return new AuthResponse(
            "Bearer",
            accessToken,
            accessTokenMinutes * 60,
            refreshToken,
            toCurrentUser(user)
        );
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public AuthResponse refresh(String rawRefreshToken) {
        RefreshTokenService.Rotation rotation =
            refreshTokenService.rotate(rawRefreshToken);

        UserAccount user = userAccountRepository
            .findWithRolesByEmailIgnoreCase(
                rotation.user().getEmail()
            )
            .orElseThrow(() -> unauthorized());

        return new AuthResponse(
            "Bearer",
            jwtService.generateAccessToken(user),
            accessTokenMinutes * 60,
            rotation.rawToken(),
            toCurrentUser(user)
        );
    }

    @Transactional
    public void logout(Long userId, String refreshToken, String ipAddress) {
        UserAccount user = userAccountRepository.findById(userId)
            .orElseThrow(() -> unauthorized());

        refreshTokenService.revoke(refreshToken, userId);

        auditService.record(
            user, "LOGOUT", "USER_ACCOUNT",
            userId, "SUCCESS", ipAddress, null
        );
    }

    @Transactional
    public void changePassword(
            Long userId,
            ChangePasswordRequest request,
            String ipAddress) {

        UserAccount user = userAccountRepository
            .findById(userId)
            .orElseThrow(() -> unauthorized());

        if (!passwordEncoder.matches(
                request.currentPassword(),
                user.getPasswordHash())) {
            auditService.record(
                user, "PASSWORD_CHANGE_FAILED", "USER_ACCOUNT",
                userId, "DENIED", ipAddress, null
            );

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Current password is incorrect"
            );
        }

        validatePasswordStrength(request.newPassword());

        user.setPasswordHash(
            passwordEncoder.encode(request.newPassword())
        );
        user.setMustChangePassword(false);
        user.setFailedLoginCount(0);
        user.setLockedUntil(null);

        userAccountRepository.save(user);
        refreshTokenService.revokeAll(userId);

        auditService.record(
            user, "PASSWORD_CHANGED", "USER_ACCOUNT",
            userId, "SUCCESS", ipAddress, null
        );
    }

    @Transactional(readOnly = true)
    public CurrentUserResponse getCurrentUser(Long userId) {
        UserAccount user = userAccountRepository
            .findWithDetailsById(userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authenticated account was not found"
            ));

        return toCurrentUser(user);
    }

    public CurrentUserResponse toCurrentUser(UserAccount user) {
        List<String> roles = user.getRoles()
            .stream()
            .map(Role::getName)
            .distinct()
            .sorted()
            .toList();

        List<String> permissions = user.getRoles()
            .stream()
            .flatMap(role -> role.getPermissions().stream())
            .map(Permission::getCode)
            .distinct()
            .sorted()
            .toList();

        Long departmentId = user.getDepartment() == null
            ? null : user.getDepartment().getId();

        String departmentName = user.getDepartment() == null
            ? null : user.getDepartment().getName();

        return new CurrentUserResponse(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            user.isEnabled(),
            roles,
            permissions,
            departmentId,
            departmentName,
            user.isMustChangePassword()
        );
    }

    private void validatePasswordStrength(String password) {
        boolean valid = password.length() >= 12
            && password.matches(".*[A-Z].*")
            && password.matches(".*[a-z].*")
            && password.matches(".*[0-9].*");

        if (!valid) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Password must contain at least 12 characters, including uppercase, lowercase and a number"
            );
        }
    }

    private ResponseStatusException unauthorized() {
        return new ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "Invalid email or password"
        );
    }
}