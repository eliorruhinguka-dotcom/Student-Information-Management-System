package rw.university.sims.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.university.sims.dto.request.DepartmentRequest;
import rw.university.sims.entity.Department;
import rw.university.sims.exception.ResourceConflictException;
import rw.university.sims.exception.ResourceNotFoundException;
import rw.university.sims.repository.DepartmentRepository;
import rw.university.sims.repository.ProgrammeRepository;

import java.util.List;

@Service
@Transactional
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final ProgrammeRepository programmeRepository;

    public DepartmentService(
            DepartmentRepository departmentRepository,
            ProgrammeRepository programmeRepository) {

        this.departmentRepository = departmentRepository;
        this.programmeRepository = programmeRepository;
    }

    // Create a department
    public Department createDepartment(DepartmentRequest request) {

        if (departmentRepository.existsByCodeIgnoreCase(
                request.code())) {

            throw new ResourceConflictException(
                    "Department code already exists");
        }

        if (departmentRepository.existsByNameIgnoreCase(
                request.name())) {

            throw new ResourceConflictException(
                    "Department name already exists");
        }

        Department department = new Department(
                request.code().trim(),
                request.name().trim()
        );

        return departmentRepository.save(department);
    }

    // Get all departments
    @Transactional(readOnly = true)
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    // Get one department
    @Transactional(readOnly = true)
    public Department getDepartmentById(Long id) {

        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Department not found with ID: " + id));
    }

    // Update a department
    public Department updateDepartment(
            Long id,
            DepartmentRequest request) {

        Department department = getDepartmentById(id);

        if (departmentRepository.existsByCodeIgnoreCaseAndIdNot(
                request.code(), id)) {

            throw new ResourceConflictException(
                    "Another department already uses this code");
        }

        if (departmentRepository.existsByNameIgnoreCaseAndIdNot(
                request.name(), id)) {

            throw new ResourceConflictException(
                    "Another department already uses this name");
        }

        department.setCode(request.code().trim());
        department.setName(request.name().trim());

        return departmentRepository.save(department);
    }

    // Deactivate a department instead of deleting it
    public Department deactivateDepartment(Long id) {

        Department department = getDepartmentById(id);

        if (!department.isActive()) {
            throw new ResourceConflictException(
                    "Department is already inactive");
        }

        long activeProgrammes =
                programmeRepository.countByDepartment_IdAndActiveTrue(id);

        if (activeProgrammes > 0) {
            throw new ResourceConflictException(
                    "Department cannot be deactivated while active programmes exist");
        }

        department.setActive(false);

        return departmentRepository.save(department);
    }
}