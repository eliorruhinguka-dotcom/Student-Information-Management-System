package rw.university.sims.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import rw.university.sims.dto.request.ProgrammeRequest;
import rw.university.sims.entity.Department;
import rw.university.sims.entity.Programme;
import rw.university.sims.exception.ResourceConflictException;
import rw.university.sims.exception.ResourceNotFoundException;
import rw.university.sims.repository.DepartmentRepository;
import rw.university.sims.repository.ProgrammeRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgrammeServiceTest {

    @Mock
    private ProgrammeRepository programmeRepository;

    @Mock
    private DepartmentRepository departmentRepository;

    @InjectMocks
    private ProgrammeService programmeService;

    private Department createDepartment() {
        return new Department("CS", "Computer Science");
    }

    private ProgrammeRequest createRequest() {
        return new ProgrammeRequest(
                "BSC-CS",
                "Bachelor of Computer Science",
                1L,
                (short) 4
        );
    }

    @Test
    void shouldCreateProgramme() {

        Department department = createDepartment();

        when(programmeRepository.existsByCodeIgnoreCase("BSC-CS"))
                .thenReturn(false);

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        when(programmeRepository.save(any(Programme.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Programme result =
                programmeService.createProgramme(createRequest());

        assertNotNull(result);
        assertEquals("BSC-CS", result.getCode());
        assertEquals("Bachelor of Computer Science", result.getName());
        assertSame(department, result.getDepartment());
        assertEquals((short) 4, result.getDurationYears());
        assertTrue(result.isActive());

        verify(programmeRepository).save(any(Programme.class));
    }

    @Test
    void shouldRejectDuplicateProgrammeCode() {

        when(programmeRepository.existsByCodeIgnoreCase("BSC-CS"))
                .thenReturn(true);

        assertThrows(
                ResourceConflictException.class,
                () -> programmeService.createProgramme(createRequest()));

        verify(programmeRepository, never())
                .save(any(Programme.class));
    }

    @Test
    void shouldRejectProgrammeWhenDepartmentDoesNotExist() {

        when(programmeRepository.existsByCodeIgnoreCase("BSC-CS"))
                .thenReturn(false);

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> programmeService.createProgramme(createRequest()));

        verify(programmeRepository, never())
                .save(any(Programme.class));
    }

    @Test
    void shouldRejectProgrammeForInactiveDepartment() {

        Department department = createDepartment();
        department.setActive(false);

        when(programmeRepository.existsByCodeIgnoreCase("BSC-CS"))
                .thenReturn(false);

        when(departmentRepository.findById(1L))
                .thenReturn(Optional.of(department));

        assertThrows(
                ResourceConflictException.class,
                () -> programmeService.createProgramme(createRequest()));

        verify(programmeRepository, never())
                .save(any(Programme.class));
    }

    @Test
    void shouldGetProgrammesByDepartment() {

        Programme programme = new Programme(
                "BSC-CS",
                "Bachelor of Computer Science",
                createDepartment(),
                (short) 4
        );

        when(departmentRepository.existsById(1L))
                .thenReturn(true);

        when(programmeRepository.findByDepartment_Id(1L))
                .thenReturn(List.of(programme));

        List<Programme> result =
                programmeService.getProgrammes(1L);

        assertEquals(1, result.size());
        assertEquals("BSC-CS", result.get(0).getCode());
    }

    @Test
    void shouldRejectProgrammeSearchForMissingDepartment() {

        when(departmentRepository.existsById(99L))
                .thenReturn(false);

        assertThrows(
                ResourceNotFoundException.class,
                () -> programmeService.getProgrammes(99L));

        verify(programmeRepository, never())
                .findByDepartment_Id(99L);
    }

    @Test
    void shouldRejectDeactivationOfAlreadyInactiveProgramme() {

        Programme programme = new Programme(
                "BSC-CS",
                "Bachelor of Computer Science",
                createDepartment(),
                (short) 4
        );

        programme.setActive(false);

        when(programmeRepository.findById(1L))
                .thenReturn(Optional.of(programme));

        assertThrows(
                ResourceConflictException.class,
                () -> programmeService.deactivateProgramme(1L));

        verify(programmeRepository, never())
                .save(any(Programme.class));
    }

    @Test
    void shouldDeactivateProgramme() {

        Programme programme = new Programme(
                "BSC-CS",
                "Bachelor of Computer Science",
                createDepartment(),
                (short) 4
        );

        when(programmeRepository.findById(1L))
                .thenReturn(Optional.of(programme));

        when(programmeRepository.save(any(Programme.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Programme result =
                programmeService.deactivateProgramme(1L);

        assertFalse(result.isActive());

        verify(programmeRepository).save(programme);
    }
}