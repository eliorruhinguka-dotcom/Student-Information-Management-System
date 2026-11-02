package rw.university.sims.service;

import rw.university.sims.dto.request.RegistrationPeriodRequest;
import rw.university.sims.entity.RegistrationPeriod;
import rw.university.sims.entity.Semester;
import rw.university.sims.enums.RegistrationPeriodStatus;
import rw.university.sims.exception.ResourceConflictException;
import rw.university.sims.repository.RegistrationPeriodRepository;
import rw.university.sims.repository.SemesterRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationPeriodServiceTest {

    @Mock
    private SemesterRepository semesterRepository;

    @Mock
    private RegistrationPeriodRepository registrationPeriodRepository;

    @InjectMocks
    private RegistrationPeriodService registrationPeriodService;

    private Semester semester;

    @BeforeEach
    void setUp() {
        semester = new Semester(
            "Semester 1",
            "2026/2027",
            LocalDate.of(2026, 9, 1),
            LocalDate.of(2027, 1, 31),
            (short) 30
        );
        semester.setId(1L);
    }

    private RegistrationPeriodRequest futureRequest() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        RegistrationPeriodRequest request = new RegistrationPeriodRequest();
        request.setOpensAt(now.plusDays(2));
        request.setClosesAt(now.plusDays(5));
        return request;
    }

    @Test
    void createFuturePeriodShouldSaveItAsScheduled() {
        when(semesterRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(semester));

        when(registrationPeriodRepository
                .findAllBySemester_IdOrderByOpensAtAsc(1L))
            .thenReturn(new ArrayList<>());

        when(registrationPeriodRepository
                .existsBySemester_IdAndStatus(
                    1L, RegistrationPeriodStatus.OPEN
                ))
            .thenReturn(false);

        when(registrationPeriodRepository.save(
                any(RegistrationPeriod.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        RegistrationPeriod result =
            registrationPeriodService.createRegistrationPeriod(
                1L, futureRequest()
            );

        assertEquals(
            RegistrationPeriodStatus.SCHEDULED,
            result.getStatus()
        );

        assertEquals(semester, result.getSemester());
        verify(registrationPeriodRepository)
            .save(any(RegistrationPeriod.class));
    }

    @Test
    void createPeriodShouldRejectInvalidDateRange() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        RegistrationPeriodRequest request =
            new RegistrationPeriodRequest();

        request.setOpensAt(now.plusDays(3));
        request.setClosesAt(now.plusDays(2));

        assertThrows(
            IllegalArgumentException.class,
            () -> registrationPeriodService.createRegistrationPeriod(
                1L, request
            )
        );

        verifyNoInteractions(
            semesterRepository,
            registrationPeriodRepository
        );
    }

    @Test
    void createPeriodShouldRejectWhenAnotherPeriodIsOpen() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        RegistrationPeriod existing = new RegistrationPeriod(
            semester,
            now.minusHours(1),
            now.plusHours(3),
            RegistrationPeriodStatus.OPEN
        );

        when(semesterRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(semester));

        when(registrationPeriodRepository
                .findAllBySemester_IdOrderByOpensAtAsc(1L))
            .thenReturn(new ArrayList<>(List.of(existing)));

        when(registrationPeriodRepository
                .existsBySemester_IdAndStatus(
                    1L, RegistrationPeriodStatus.OPEN
                ))
            .thenReturn(true);

        RegistrationPeriodRequest request =
            new RegistrationPeriodRequest();

        request.setOpensAt(now);
        request.setClosesAt(now.plusDays(1));

        assertThrows(
            ResourceConflictException.class,
            () -> registrationPeriodService.createRegistrationPeriod(
                1L, request
            )
        );

        verify(registrationPeriodRepository, never())
            .save(argThat(period ->
                period.getStatus() == RegistrationPeriodStatus.OPEN
            ));
    }

    @Test
    void closePeriodShouldChangeOpenPeriodToClosed() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        RegistrationPeriod period = new RegistrationPeriod(
            semester,
            now.minusHours(1),
            now.plusHours(3),
            RegistrationPeriodStatus.OPEN
        );
        period.setId(10L);

        when(registrationPeriodRepository.findById(10L))
            .thenReturn(Optional.of(period));

        when(semesterRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(semester));

        when(registrationPeriodRepository
                .findAllBySemester_IdOrderByOpensAtAsc(1L))
            .thenReturn(new ArrayList<>(List.of(period)));

        when(registrationPeriodRepository
                .existsBySemester_IdAndStatus(
                    1L, RegistrationPeriodStatus.OPEN
                ))
            .thenReturn(true);

        when(registrationPeriodRepository.save(
                any(RegistrationPeriod.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        RegistrationPeriod result =
            registrationPeriodService.closeRegistrationPeriod(10L);

        assertEquals(
            RegistrationPeriodStatus.CLOSED,
            result.getStatus()
        );
        assertNotNull(result.getClosedAt());
    }

    @Test
    void closePeriodShouldRejectScheduledPeriod() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        RegistrationPeriod period = new RegistrationPeriod(
            semester,
            now.plusDays(2),
            now.plusDays(4),
            RegistrationPeriodStatus.SCHEDULED
        );
        period.setId(11L);

        when(registrationPeriodRepository.findById(11L))
            .thenReturn(Optional.of(period));

        when(semesterRepository.findByIdForUpdate(1L))
            .thenReturn(Optional.of(semester));

        when(registrationPeriodRepository
                .findAllBySemester_IdOrderByOpensAtAsc(1L))
            .thenReturn(new ArrayList<>(List.of(period)));

        when(registrationPeriodRepository
                .existsBySemester_IdAndStatus(
                    1L, RegistrationPeriodStatus.OPEN
                ))
            .thenReturn(false);

        assertThrows(
            ResourceConflictException.class,
            () -> registrationPeriodService.closeRegistrationPeriod(11L)
        );
    }
}