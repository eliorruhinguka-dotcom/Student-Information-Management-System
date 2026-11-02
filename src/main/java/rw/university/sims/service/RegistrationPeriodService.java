package rw.university.sims.service;

import rw.university.sims.dto.request.RegistrationPeriodRequest;
import rw.university.sims.entity.RegistrationPeriod;
import rw.university.sims.entity.Semester;
import rw.university.sims.enums.RegistrationPeriodStatus;
import rw.university.sims.exception.ResourceConflictException;
import rw.university.sims.exception.ResourceNotFoundException;
import rw.university.sims.repository.RegistrationPeriodRepository;
import rw.university.sims.repository.SemesterRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class RegistrationPeriodService {

    private final SemesterRepository semesterRepository;
    private final RegistrationPeriodRepository registrationPeriodRepository;

    public RegistrationPeriodService(
            SemesterRepository semesterRepository,
            RegistrationPeriodRepository registrationPeriodRepository) {
        this.semesterRepository = semesterRepository;
        this.registrationPeriodRepository = registrationPeriodRepository;
    }

    @Transactional
    public RegistrationPeriod createRegistrationPeriod(
            Long semesterId,
            RegistrationPeriodRequest request) {

        validateWindow(request);

        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        if (!request.getClosesAt().isAfter(now)) {
            throw new IllegalArgumentException(
                "Registration period closing time must be in the future"
            );
        }

        Semester semester = lockSemester(semesterId);
        synchronizeStatuses(semester, now);

        RegistrationPeriodStatus initialStatus =
            request.getOpensAt().isAfter(now)
                ? RegistrationPeriodStatus.SCHEDULED
                : RegistrationPeriodStatus.OPEN;

        if (initialStatus == RegistrationPeriodStatus.OPEN
                && registrationPeriodRepository.existsBySemester_IdAndStatus(
                    semesterId,
                    RegistrationPeriodStatus.OPEN)) {
            throw new ResourceConflictException(
                "This semester already has an open registration period"
            );
        }

        RegistrationPeriod period = new RegistrationPeriod(
            semester,
            request.getOpensAt(),
            request.getClosesAt(),
            initialStatus
        );

        return registrationPeriodRepository.save(period);
    }

    @Transactional
    public RegistrationPeriod closeRegistrationPeriod(Long id) {
        RegistrationPeriod existing = registrationPeriodRepository.findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Registration period not found: " + id
                )
            );

        Semester semester = lockSemester(existing.getSemester().getId());
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        synchronizeStatuses(semester, now);

        RegistrationPeriod period = registrationPeriodRepository.findById(id)
            .orElseThrow(() ->
                new ResourceNotFoundException(
                    "Registration period not found: " + id
                )
            );

        if (period.getStatus() != RegistrationPeriodStatus.OPEN) {
            throw new ResourceConflictException(
                "Only an open registration period can be closed"
            );
        }

        period.setStatus(RegistrationPeriodStatus.CLOSED);
        period.setClosedAt(now);

        return registrationPeriodRepository.save(period);
    }

    @Transactional
    public List<RegistrationPeriod> getPeriodsBySemester(Long semesterId) {
        Semester semester = lockSemester(semesterId);
        synchronizeStatuses(semester, OffsetDateTime.now(ZoneOffset.UTC));

        return registrationPeriodRepository
            .findAllBySemester_IdOrderByOpensAtAsc(semesterId);
    }

    private Semester lockSemester(Long semesterId) {
        return semesterRepository.findByIdForUpdate(semesterId)
            .orElseThrow(() ->
                new ResourceNotFoundException("Semester not found: " + semesterId)
            );
    }

    private void validateWindow(RegistrationPeriodRequest request) {
        if (!request.getClosesAt().isAfter(request.getOpensAt())) {
            throw new IllegalArgumentException(
                "Registration period closing time must be after its opening time"
            );
        }
    }

    /*
     * Future periods become OPEN when their opening time arrives.
     * Expired OPEN periods become CLOSED.
     *
     * This synchronization runs whenever registration periods are
     * created, closed or listed. The later course-registration service
     * must also synchronize periods before allowing student registration.
     */
    private void synchronizeStatuses(
            Semester semester,
            OffsetDateTime now) {

        List<RegistrationPeriod> periods =
            registrationPeriodRepository
                .findAllBySemester_IdOrderByOpensAtAsc(semester.getId());

        for (RegistrationPeriod period : periods) {
            if (period.getStatus() == RegistrationPeriodStatus.OPEN
                    && !period.getClosesAt().isAfter(now)) {
                period.setStatus(RegistrationPeriodStatus.CLOSED);
                period.setClosedAt(period.getClosesAt());
                registrationPeriodRepository.save(period);
            }
        }

        boolean hasOpenPeriod = registrationPeriodRepository
            .existsBySemester_IdAndStatus(
                semester.getId(),
                RegistrationPeriodStatus.OPEN
            );

        if (!hasOpenPeriod) {
            for (RegistrationPeriod period : periods) {
                boolean isReadyToOpen =
                    period.getStatus() == RegistrationPeriodStatus.SCHEDULED
                    && !period.getOpensAt().isAfter(now)
                    && period.getClosesAt().isAfter(now);

                if (isReadyToOpen) {
                    period.setStatus(RegistrationPeriodStatus.OPEN);
                    registrationPeriodRepository.save(period);
                    break;
                }
            }
        }
    }
}