package rw.university.sims.repository;

import rw.university.sims.entity.RegistrationPeriod;
import rw.university.sims.enums.RegistrationPeriodStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistrationPeriodRepository
        extends JpaRepository<RegistrationPeriod, Long> {

    List<RegistrationPeriod> findAllBySemester_IdOrderByOpensAtAsc(
        Long semesterId
    );

    boolean existsBySemester_IdAndStatus(
        Long semesterId,
        RegistrationPeriodStatus status
    );
}