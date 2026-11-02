package rw.university.sims.repository;

import rw.university.sims.entity.StudentUserLink;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StudentUserLinkRepository
        extends JpaRepository<StudentUserLink, Long> {

    Optional<StudentUserLink> findByStudent_Id(Long studentId);

    boolean existsByStudent_Id(Long studentId);
}