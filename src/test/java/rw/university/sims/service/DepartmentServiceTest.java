package rw.university.sims.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import rw.university.sims.dto.request.DepartmentRequest;
import rw.university.sims.entity.Department;
import rw.university.sims.exception.ResourceConflictException;
import rw.university.sims.exception.ResourceNotFoundException;
import rw.university.sims.repository.DepartmentRepository;
import rw.university.sims.repository.ProgrammeRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DepartmentServiceTest {

    @Mock
    private DepartmentRepository departmentRepository;

    @Mock
    private ProgrammeRepository programmeRepository;

    @InjectMocks
    private DepartmentService departmentService;

    @Test
    void shouldCreateDepartment() {

        DepartmentRequest request =
                new DepartmentRequest("CS", "Computer Science");

        when(departmentRepository.existsByCodeIgnoreCase("CS"))
                .thenReturn(false);

        when(departmentRepository.existsByNameIgnoreCase(
                "Computer Science")).thenReturn(false);

        when(departmentRepository.save(any(Department.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Department result =
                departmentService.createDepartment(request);

        assertNotNull(result);
        assertEquals("CS", result.getCode());
        assertEquals("Computer Science", result.getName());
        assertTrue(result.isActive());

        verify(departmentRepository).save(any(Department.class));
    }

    @Test
    void shouldRejectDuplicateDepartmentCode() {

        DepartmentRequest request =
                new DepartmentRequest("CS", "Computing");

        when(departmentRepository.existsByCodeIgnoreCase("CS"))
                .thenReturn(true);

        assertThrows(
                ResourceConflictException.class,
                () -> departmentService.createDepartment(request));

        verify(departmentRepository, never())
                .save(any(Department.class));
    }

    @Test
    void shouldRejectDuplicateDepartmentName() {

        DepartmentRequest request =
                new DepartmentRequest("IT", "Computer Science");

        when(departmentRepository.existsByCodeIgnoreCase("IT"))
                .thenReturn(false);

        when(departmentRepository.existsByNameIgnoreCase(
                "Computer Science")).thenReturn(true);

        assertThrows(
                ResourceConflictException.class,
                () -> departmentService.createDepartment(request));

        verify(departmentRepository, never())
                .save(any(Department.class));
    }

    @Test
    void shouldGetDepartmentById() {

        Department department =
                new Department("CS", "Computer Science");

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        Department result =
                departmentService.getDepartmentById(1L);

        assertSame(department, result);
    }

    @Test
    void shouldThrowWhenDepartmentDoesNotExist() {

        when(departmentRepository.findById(99L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> departmentService.getDepartmentById(99L));
    }

    @Test
    void shouldRejectUpdateWhenCodeBelongsToAnotherDepartment() {

        Department department =
                new Department("CS", "Computer Science");

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        when(departmentRepository.existsByCodeIgnoreCaseAndIdNot(
                "IT", 1L)).thenReturn(true);

        DepartmentRequest request =
                new DepartmentRequest("IT", "Information Technology");

        assertThrows(
                ResourceConflictException.class,
                () -> departmentService.updateDepartment(1L, request));

        verify(departmentRepository, never())
                .save(any(Department.class));
    }

    @Test
    void shouldNotDeactivateDepartmentWithActiveProgrammes() {

        Department department =
                new Department("CS", "Computer Science");

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        when(programmeRepository.countByDepartment_IdAndActiveTrue(1L))
                .thenReturn(2L);

        assertThrows(
                ResourceConflictException.class,
                () -> departmentService.deactivateDepartment(1L));

        verify(departmentRepository, never())
                .save(any(Department.class));
    }

    @Test
    void shouldDeactivateDepartmentWithoutActiveProgrammes() {

        Department department =
                new Department("CS", "Computer Science");

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        when(programmeRepository.countByDepartment_IdAndActiveTrue(1L))
                .thenReturn(0L);

        when(departmentRepository.save(any(Department.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Department result =
                departmentService.deactivateDepartment(1L);

        assertFalse(result.isActive());

        verify(departmentRepository).save(department);
    }
}