package rw.university.sims.repository;

import rw.university.sims.entity.UserAccount;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserAccountRepository
        extends JpaRepository<UserAccount, Long>,
                JpaSpecificationExecutor<UserAccount> {

    Optional<UserAccount> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = {
        "roles",
        "roles.permissions",
        "department",
        "studentLink"
    })
    Optional<UserAccount> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {
        "roles",
        "roles.permissions",
        "department"
    })
    Optional<UserAccount> findWithRolesByEmailIgnoreCase(String email);
}