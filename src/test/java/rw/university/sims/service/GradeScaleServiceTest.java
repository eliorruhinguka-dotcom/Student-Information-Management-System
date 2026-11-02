package rw.university.sims.service;

import org.junit.jupiter.api.Test;
import rw.university.sims.dto.request.*;
import rw.university.sims.entity.Role;
import rw.university.sims.entity.UserAccount;
import rw.university.sims.repository.AcademicResultRepository;
import rw.university.sims.repository.GradeScaleRepository;
import rw.university.sims.repository.UserAccountRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GradeScaleServiceTest {

    @Test
    void createScalePersistsBandsAndLeavesScaleInactiveUntilActivated() {

        GradeScaleRepository scales = mock(GradeScaleRepository.class);
        AcademicResultRepository results = mock(AcademicResultRepository.class);
        UserAccountRepository users = mock(UserAccountRepository.class);
        AuditService audit = mock(AuditService.class);

        UserAccount actor = new UserAccount();
        actor.setId(7L);

        Role registrar = new Role();
        registrar.setName("REGISTRAR");
        actor.setRoles(Set.of(registrar));

        when(users.findWithDetailsById(7L))
                .thenReturn(Optional.of(actor));

        when(scales.existsByNameIgnoreCase("UGL 2026"))
                .thenReturn(false);

        when(scales.saveAndFlush(any())).thenAnswer(invocation -> {
            var scale = (rw.university.sims.entity.GradeScale)
                    invocation.getArgument(0);

            assertFalse(scale.isActive());
            assertEquals(2, scale.getBands().size());

            return scale;
        });

        var request = new GradeScaleCreateRequest(
                "UGL 2026",
                LocalDate.of(2026, 9, 1),
                List.of(
                        new GradeScaleBandRequest(
                                new BigDecimal("0"),
                                new BigDecimal("49.99"),
                                "F",
                                new BigDecimal("0.00"),
                                false
                        ),
                        new GradeScaleBandRequest(
                                new BigDecimal("50"),
                                new BigDecimal("100"),
                                "P",
                                new BigDecimal("2.00"),
                                true
                        )
                )
        );

        var result = new GradeScaleService(
                scales, results, users, audit
        ).create(request, 7L, "127.0.0.1");

        assertEquals("UGL 2026", result.name());
        assertFalse(result.active());
        assertEquals(2, result.bands().size());

        verify(audit).record(
                actor,
                "GRADE_SCALE_CREATED",
                "GRADE_SCALE",
                null,
                "SUCCESS",
                "127.0.0.1",
                null
        );
    }

    @Test
    void overlappingBandsAreRejected() {

        GradeScaleRepository scales = mock(GradeScaleRepository.class);
        AcademicResultRepository results = mock(AcademicResultRepository.class);
        UserAccountRepository users = mock(UserAccountRepository.class);
        AuditService audit = mock(AuditService.class);

        UserAccount actor = new UserAccount();

        Role registrar = new Role();
        registrar.setName("REGISTRAR");
        actor.setRoles(Set.of(registrar));

        when(users.findWithDetailsById(1L))
                .thenReturn(Optional.of(actor));

        when(scales.existsByNameIgnoreCase("bad"))
                .thenReturn(false);

        GradeScaleService service = new GradeScaleService(
                scales, results, users, audit
        );

        var bands = List.of(
                new GradeScaleBandRequest(
                        new BigDecimal("0"),
                        new BigDecimal("60"),
                        "F",
                        BigDecimal.ZERO,
                        false
                ),
                new GradeScaleBandRequest(
                        new BigDecimal("60"),
                        new BigDecimal("100"),
                        "P",
                        BigDecimal.ONE,
                        true
                )
        );

        assertThrows(
                RuntimeException.class,
                () -> service.create(
                        new GradeScaleCreateRequest(
                                "bad",
                                LocalDate.now(),
                                bands
                        ),
                        1L,
                        null
                )
        );
    }
}