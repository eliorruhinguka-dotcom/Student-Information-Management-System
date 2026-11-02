package rw.university.sims.config;

import rw.university.sims.entity.Role;
import rw.university.sims.entity.UserAccount;
import rw.university.sims.repository.RoleRepository;
import rw.university.sims.repository.UserAccountRepository;
import rw.university.sims.service.AuditService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
public class BootstrapAdminInitializer implements ApplicationRunner {

    private final UserAccountRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final String email;
    private final String fullName;
    private final String password;

    public BootstrapAdminInitializer(
            UserAccountRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            AuditService auditService,
            @Value("${security.bootstrap-admin.email:}") String email,
            @Value("${security.bootstrap-admin.full-name:}") String fullName,
            @Value("${security.bootstrap-admin.password:}") String password) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
        this.email = email;
        this.fullName = fullName;
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments arguments) {
        if (email.isBlank() && password.isBlank() && fullName.isBlank()) {
            return;
        }

        if (email.isBlank() || password.isBlank() || fullName.isBlank()) {
            throw new IllegalStateException(
                "All three bootstrap administrator environment values must be provided"
            );
        }

        if (userRepository.count() > 0) {
            return;
        }

        if (password.length() < 12
                || !password.matches(".*[A-Z].*")
                || !password.matches(".*[a-z].*")
                || !password.matches(".*[0-9].*")) {
            throw new IllegalStateException(
                "Bootstrap password must have at least 12 characters, including uppercase, lowercase and a number"
            );
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalStateException(
                "Bootstrap administrator email already exists"
            );
        }

        Role adminRole = roleRepository.findByName("SYSTEM_ADMINISTRATOR")
            .orElseThrow(() ->
                new IllegalStateException(
                    "SYSTEM_ADMINISTRATOR role was not seeded by V8"
                )
            );

        UserAccount admin = new UserAccount();
        admin.setEmail(email.trim().toLowerCase());
        admin.setFullName(fullName.trim());
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setEnabled(true);
        admin.setMustChangePassword(true);
        admin.setRoles(Set.of(adminRole));

        UserAccount saved = userRepository.save(admin);

        auditService.record(
            null,
            "INITIAL_ADMIN_CREATED",
            "USER_ACCOUNT",
            saved.getId(),
            "SUCCESS",
            null,
            null
        );
    }
}