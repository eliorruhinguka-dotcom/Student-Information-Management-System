package rw.university.sims.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import rw.university.sims.dto.request.DepartmentRequest;
import rw.university.sims.dto.response.DepartmentResponse;
import rw.university.sims.dto.response.GenericResponse;
import rw.university.sims.entity.Department;
import rw.university.sims.service.DepartmentService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    // POST /api/v1/departments
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GenericResponse<DepartmentResponse> createDepartment(
            @Valid @RequestBody DepartmentRequest request) {

        Department department =
                departmentService.createDepartment(request);

        return GenericResponse.success(
                201,
                "Department created successfully",
                toResponse(department)
        );
    }

    // GET /api/v1/departments
    @GetMapping
    public GenericResponse<List<DepartmentResponse>> getAllDepartments() {

        List<DepartmentResponse> departments =
                departmentService.getAllDepartments()
                        .stream()
                        .map(this::toResponse)
                        .toList();

        return GenericResponse.success(
                200,
                "Departments retrieved successfully",
                departments
        );
    }

    // GET /api/v1/departments/{id}
    @GetMapping("/{id}")
    public GenericResponse<DepartmentResponse> getDepartmentById(
            @PathVariable Long id) {

        Department department =
                departmentService.getDepartmentById(id);

        return GenericResponse.success(
                200,
                "Department retrieved successfully",
                toResponse(department)
        );
    }

    // PUT /api/v1/departments/{id}
    @PutMapping("/{id}")
    public GenericResponse<DepartmentResponse> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {

        Department department =
                departmentService.updateDepartment(id, request);

        return GenericResponse.success(
                200,
                "Department updated successfully",
                toResponse(department)
        );
    }

    // PATCH /api/v1/departments/{id}/deactivate
    @PatchMapping("/{id}/deactivate")
    public GenericResponse<DepartmentResponse> deactivateDepartment(
            @PathVariable Long id) {

        Department department =
                departmentService.deactivateDepartment(id);

        return GenericResponse.success(
                200,
                "Department deactivated successfully",
                toResponse(department)
        );
    }

    private DepartmentResponse toResponse(Department department) {

        return new DepartmentResponse(
                department.getId(),
                department.getCode(),
                department.getName(),
                department.isActive()
        );
    }
}