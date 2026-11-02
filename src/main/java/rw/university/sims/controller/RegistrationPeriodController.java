package rw.university.sims.controller;

import jakarta.validation.Valid;
import rw.university.sims.dto.request.RegistrationPeriodRequest;
import rw.university.sims.dto.response.GenericResponse;
import rw.university.sims.dto.response.RegistrationPeriodResponse;
import rw.university.sims.entity.RegistrationPeriod;
import rw.university.sims.service.RegistrationPeriodService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class RegistrationPeriodController {

    private final RegistrationPeriodService registrationPeriodService;

    public RegistrationPeriodController(
            RegistrationPeriodService registrationPeriodService) {
        this.registrationPeriodService = registrationPeriodService;
    }

    @PostMapping("/semesters/{semesterId}/registration-periods")
    @ResponseStatus(HttpStatus.CREATED)
    public GenericResponse<RegistrationPeriodResponse> createPeriod(
            @PathVariable Long semesterId,
            @Valid @RequestBody RegistrationPeriodRequest request) {

        RegistrationPeriod period =
            registrationPeriodService.createRegistrationPeriod(
                semesterId,
                request
            );

        return GenericResponse.success(
            201,
            "Registration period created successfully",
            toResponse(period)
        );
    }

    @PostMapping("/registration-periods/{id}/close")
    public GenericResponse<RegistrationPeriodResponse> closePeriod(
            @PathVariable Long id) {

        RegistrationPeriod period =
            registrationPeriodService.closeRegistrationPeriod(id);

        return GenericResponse.success(
            200,
            "Registration period closed successfully",
            toResponse(period)
        );
    }

    @GetMapping("/semesters/{semesterId}/registration-periods")
    public GenericResponse<List<RegistrationPeriodResponse>> getPeriods(
            @PathVariable Long semesterId) {

        List<RegistrationPeriodResponse> periods =
            registrationPeriodService.getPeriodsBySemester(semesterId)
                .stream()
                .map(this::toResponse)
                .toList();

        return GenericResponse.success(
            200,
            "Registration periods retrieved successfully",
            periods
        );
    }

    private RegistrationPeriodResponse toResponse(
            RegistrationPeriod period) {

        return new RegistrationPeriodResponse(
            period.getId(),
            period.getSemester().getId(),
            period.getOpensAt(),
            period.getClosesAt(),
            period.getStatus(),
            period.getOpenedBy(),
            period.getClosedBy(),
            period.getClosedAt()
        );
    }
}