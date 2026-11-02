package rw.university.sims.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import rw.university.sims.dto.request.ProgrammeRequest;
import rw.university.sims.dto.response.GenericResponse;
import rw.university.sims.dto.response.ProgrammeResponse;
import rw.university.sims.entity.Programme;
import rw.university.sims.service.ProgrammeService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/programmes")
public class ProgrammeController {

    private final ProgrammeService programmeService;

    public ProgrammeController(ProgrammeService programmeService) {
        this.programmeService = programmeService;
    }

    // POST /api/v1/programmes
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GenericResponse<ProgrammeResponse> createProgramme(
            @Valid @RequestBody ProgrammeRequest request) {

        Programme programme =
                programmeService.createProgramme(request);

        return GenericResponse.success(
                201,
                "Programme created successfully",
                toResponse(programme)
        );
    }

    // GET /api/v1/programmes?departmentId=1
    @GetMapping
    public GenericResponse<List<ProgrammeResponse>> getProgrammes(
            @RequestParam(required = false) Long departmentId) {

        List<ProgrammeResponse> programmes =
                programmeService.getProgrammes(departmentId)
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return GenericResponse.success(
                200,
                "Programmes retrieved successfully",
                programmes
        );
    }

    // PUT /api/v1/programmes/{id}
    @PutMapping("/{id}")
    public GenericResponse<ProgrammeResponse> updateProgramme(
            @PathVariable Long id,
            @Valid @RequestBody ProgrammeRequest request) {

        Programme programme =
                programmeService.updateProgramme(id, request);

        return GenericResponse.success(
                200,
                "Programme updated successfully",
                toResponse(programme)
        );
    }

    // PATCH /api/v1/programmes/{id}/deactivate
    @PatchMapping("/{id}/deactivate")
    public GenericResponse<ProgrammeResponse> deactivateProgramme(
            @PathVariable Long id) {

        Programme programme =
                programmeService.deactivateProgramme(id);

        return GenericResponse.success(
                200,
                "Programme deactivated successfully",
                toResponse(programme)
        );
    }

    private ProgrammeResponse toResponse(Programme programme) {

        return new ProgrammeResponse(
                programme.getId(),
                programme.getCode(),
                programme.getName(),
                programme.getDepartment().getId(),
                programme.getDepartment().getCode(),
                programme.getDepartment().getName(),
                programme.getDurationYears(),
                programme.isActive()
        );
    }
}