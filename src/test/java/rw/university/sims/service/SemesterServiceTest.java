package rw.university.sims.service;

import rw.university.sims.dto.request.SemesterRequest;
import rw.university.sims.entity.Semester;
import rw.university.sims.exception.ResourceConflictException;
import rw.university.sims.repository.SemesterRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SemesterServiceTest {

    @Mock
    private SemesterRepository semesterRepository;

    @InjectMocks
    private SemesterService semesterService;

    private SemesterRequest validRequest() {
        SemesterRequest request = new SemesterRequest();
        request.setName("Semester 1");
        request.setAcademicYear("2026/2027");
        request.setStartDate(LocalDate.of(2026, 9, 1));
        request.setEndDate(LocalDate.of(2027, 1, 31));
        request.setMaxCreditLoad((short) 30);
        return request;
    }

    @Test
    void createSemesterShouldSaveValidSemester() {
        SemesterRequest request = validRequest();

        when(semesterRepository.existsByNameIgnoreCaseAndAcademicYear(
            "Semester 1", "2026/2027"
        )).thenReturn(false);

        when(semesterRepository.save(any(Semester.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Semester result = semesterService.createSemester(request);

        assertEquals("Semester 1", result.getName());
        assertEquals("2026/2027", result.getAcademicYear());
        assertEquals((short) 30, result.getMaxCreditLoad());

        verify(semesterRepository).save(any(Semester.class));
    }

    @Test
    void createSemesterShouldRejectDuplicateNameAndYear() {
        SemesterRequest request = validRequest();

        when(semesterRepository.existsByNameIgnoreCaseAndAcademicYear(
            "Semester 1", "2026/2027"
        )).thenReturn(true);

        assertThrows(
            ResourceConflictException.class,
            () -> semesterService.createSemester(request)
        );

        verify(semesterRepository, never()).save(any());
    }

    @Test
    void createSemesterShouldRejectEndDateBeforeStartDate() {
        SemesterRequest request = validRequest();
        request.setEndDate(LocalDate.of(2026, 8, 31));

        assertThrows(
            IllegalArgumentException.class,
            () -> semesterService.createSemester(request)
        );

        verifyNoInteractions(semesterRepository);
    }

    @Test
    void getSemesterShouldReturnExistingSemester() {
        Semester semester = new Semester(
            "Semester 1",
            "2026/2027",
            LocalDate.of(2026, 9, 1),
            LocalDate.of(2027, 1, 31),
            (short) 30
        );

        when(semesterRepository.findById(1L))
            .thenReturn(Optional.of(semester));

        assertSame(semester, semesterService.getSemesterById(1L));
    }

    @Test
    void getAllSemestersShouldReturnRepositoryResults() {
        List<Semester> semesters = List.of(
            new Semester(
                "Semester 1",
                "2026/2027",
                LocalDate.of(2026, 9, 1),
                LocalDate.of(2027, 1, 31),
                (short) 30
            )
        );

        when(semesterRepository.findAllByOrderByStartDateDesc())
            .thenReturn(semesters);

        assertEquals(1, semesterService.getAllSemesters().size());
    }
}