package rw.university.sims.controller;

import rw.university.sims.dto.request.RegistrationPeriodRequest;
import rw.university.sims.dto.response.GenericResponse;
import rw.university.sims.dto.response.RegistrationPeriodResponse;
import rw.university.sims.entity.RegistrationPeriod;
import rw.university.sims.entity.Semester;
import rw.university.sims.enums.RegistrationPeriodStatus;
import rw.university.sims.service.RegistrationPeriodService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationPeriodControllerTest {

    @Mock
    private RegistrationPeriodService registrationPeriodService;

    @InjectMocks
    private RegistrationPeriodController registrationPeriodController;

    private Semester createSemester() {
        Semester semester = new Semester(
            "Semester 1",
            "2026/2027",
            LocalDate.of(2026, 9, 1),
            LocalDate.of(2027, 1, 31),
            (short) 30
        );
        semester.setId(1L);
        return semester;
    }

    private RegistrationPeriod createPeriod() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        RegistrationPeriod period = new RegistrationPeriod(
            createSemester(),
            now.plusDays(1),
            now.plusDays(5),
            RegistrationPeriodStatus.SCHEDULED
        );
        period.setId(10L);
        return period;
    }

    private RegistrationPeriodRequest createRequest() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);

        RegistrationPeriodRequest request =
            new RegistrationPeriodRequest();

        request.setOpensAt(now.plusDays(1));
        request.setClosesAt(now.plusDays(5));
        return request;
    }

    @Test
    void createPeriodShouldReturnCreatedResponse() {
        when(registrationPeriodService.createRegistrationPeriod(
            eq(1L), any(RegistrationPeriodRequest.class)
        )).thenReturn(createPeriod());

        GenericResponse<RegistrationPeriodResponse> response =
            registrationPeriodController.createPeriod(1L, createRequest());

        assertEquals(201, response.status());
        assertEquals(10L, response.data().id());
        assertEquals(1L, response.data().semesterId());
        assertEquals(
            RegistrationPeriodStatus.SCHEDULED,
            response.data().status()
        );
    }

    @Test
    void closePeriodShouldReturnClosedResponse() {
        RegistrationPeriod period = createPeriod();
        period.setStatus(RegistrationPeriodStatus.CLOSED);

        when(registrationPeriodService.closeRegistrationPeriod(10L))
            .thenReturn(period);

        GenericResponse<RegistrationPeriodResponse> response =
            registrationPeriodController.closePeriod(10L);

        assertEquals(200, response.status());
        assertEquals(
            RegistrationPeriodStatus.CLOSED,
            response.data().status()
        );
    }

    @Test
    void getPeriodsShouldReturnSemesterPeriods() {
        when(registrationPeriodService.getPeriodsBySemester(1L))
            .thenReturn(List.of(createPeriod()));

        GenericResponse<List<RegistrationPeriodResponse>> response =
            registrationPeriodController.getPeriods(1L);

        assertEquals(200, response.status());
        assertEquals(1, response.data().size());
        assertEquals(10L, response.data().get(0).id());
    }
}