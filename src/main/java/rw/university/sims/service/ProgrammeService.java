package rw.university.sims.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import rw.university.sims.dto.request.ProgrammeRequest;
import rw.university.sims.entity.Department;
import rw.university.sims.entity.Programme;
import rw.university.sims.exception.ResourceConflictException;
import rw.university.sims.exception.ResourceNotFoundException;
import rw.university.sims.repository.DepartmentRepository;
import rw.university.sims.repository.ProgrammeRepository;

import java.util.List;

@Service
@Transactional
public class ProgrammeService {

    private final ProgrammeRepository programmeRepository;
    private final DepartmentRepository departmentRepository;

    public ProgrammeService(
            ProgrammeRepository programmeRepository,
            DepartmentRepository departmentRepository) {

        this.programmeRepository = programmeRepository;
        this.departmentRepository = departmentRepository;
    }

    // Create a programme
    public Programme createProgramme(ProgrammeRequest request) {

        if (programmeRepository.existsByCodeIgnoreCase(
                request.code())) {

            throw new ResourceConflictException(
                    "Programme code already exists");
        }

        Department department =
                findActiveDepartment(request.departmentId());

        Programme programme = new Programme(
                request.code().trim(),
                request.name().trim(),
                department,
                request.durationYears()
        );

        return programmeRepository.save(programme);
    }

    // List programmes, optionally filtered by department
    @Transactional(readOnly = true)
    public List<Programme> getProgrammes(Long departmentId) {

        if (departmentId == null) {
            return programmeRepository.findAll();
        }

        if (!departmentRepository.existsById(departmentId)) {
            throw new ResourceNotFoundException(
                    "Department not found with ID: " + departmentId);
        }

        return programmeRepository.findByDepartment_Id(departmentId);
    }

    // Get one programme for internal service operations
    @Transactional(readOnly = true)
    public Programme getProgrammeById(Long id) {

        return programmeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Programme not found with ID: " + id));
    }

    // Update a programme
    public Programme updateProgramme(
            Long id,
            ProgrammeRequest request) {

        Programme programme = getProgrammeById(id);

        if (programmeRepository.existsByCodeIgnoreCaseAndIdNot(
                request.code(), id)) {

            throw new ResourceConflictException(
                    "Another programme already uses this code");
        }

        Department department =
                findActiveDepartment(request.departmentId());

        programme.setCode(request.code().trim());
        programme.setName(request.name().trim());
        programme.setDepartment(department);
        programme.setDurationYears(request.durationYears());

        return programmeRepository.save(programme);
    }

    // Deactivate a programme instead of deleting it
    public Programme deactivateProgramme(Long id) {

        Programme programme = getProgrammeById(id);

        if (!programme.isActive()) {
            throw new ResourceConflictException(
                    "Programme is already inactive");
        }

        programme.setActive(false);

        return programmeRepository.save(programme);
    }

    private Department findActiveDepartment(Long departmentId) {

        Department department =
                departmentRepository.findById(departmentId)
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Department not found with ID: "
                                        + departmentId));

        if (!department.isActive()) {
            throw new ResourceConflictException(
                    "Cannot assign a programme to an inactive department");
        }

        return department;
    }
}