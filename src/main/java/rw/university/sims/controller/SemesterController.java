package rw.university.sims.controller;

import jakarta.validation.Valid;
import rw.university.sims.dto.request.SemesterRequest;
import rw.university.sims.dto.response.GenericResponse;
import rw.university.sims.dto.response.SemesterResponse;
import rw.university.sims.entity.Semester;
import rw.university.sims.service.SemesterService;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/semesters")
public class SemesterController {

    private final SemesterService semesterService;

    public SemesterController(SemesterService semesterService) {
        this.semesterService = semesterService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GenericResponse<SemesterResponse> createSemester(
            @Valid @RequestBody SemesterRequest request) {

        Semester semester = semesterService.createSemester(request);

        return GenericResponse.success(
            201,
            "Semester created successfully",
            toResponse(semester)
        );
    }

    @GetMapping
    public GenericResponse<List<SemesterResponse>> getAllSemesters() {
        List<SemesterResponse> semesters =
            semesterService.getAllSemesters()
                .stream()
                .map(this::toResponse)
                .toList();

        return GenericResponse.success(
            200,
            "Semesters retrieved successfully",
            semesters
        );
    }

    @GetMapping("/{id}")
    public GenericResponse<SemesterResponse> getSemesterById(
            @PathVariable Long id) {

        return GenericResponse.success(
            200,
            "Semester retrieved successfully",
            toResponse(semesterService.getSemesterById(id))
        );
    }

    @PutMapping("/{id}")
    public GenericResponse<SemesterResponse> updateSemester(
            @PathVariable Long id,
            @Valid @RequestBody SemesterRequest request) {

        return GenericResponse.success(
            200,
            "Semester updated successfully",
            toResponse(semesterService.updateSemester(id, request))
        );
    }

    private SemesterResponse toResponse(Semester semester) {
        return new SemesterResponse(
            semester.getId(),
            semester.getName(),
            semester.getAcademicYear(),
            semester.getStartDate(),
            semester.getEndDate(),
            semester.getMaxCreditLoad()
        );
    }
}