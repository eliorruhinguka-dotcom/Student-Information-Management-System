package rw.university.sims.controller;

import rw.university.sims.dto.request.SemesterRequest;
import rw.university.sims.dto.response.GenericResponse;
import rw.university.sims.dto.response.SemesterResponse;
import rw.university.sims.entity.Semester;
import rw.university.sims.service.SemesterService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SemesterControllerTest {

    @Mock
    private SemesterService semesterService;

    @InjectMocks
    private SemesterController semesterController;

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

    private SemesterRequest createRequest() {
        SemesterRequest request = new SemesterRequest();
        request.setName("Semester 1");
        request.setAcademicYear("2026/2027");
        request.setStartDate(LocalDate.of(2026, 9, 1));
        request.setEndDate(LocalDate.of(2027, 1, 31));
        request.setMaxCreditLoad((short) 30);
        return request;
    }

    @Test
    void createSemesterShouldReturnCreatedResponse() {
        Semester semester = createSemester();
        when(semesterService.createSemester(any(SemesterRequest.class)))
            .thenReturn(semester);

        GenericResponse<SemesterResponse> response =
            semesterController.createSemester(createRequest());

        assertEquals(201, response.status());
        assertEquals("SUCCESS", response.code());
        assertEquals(1L, response.data().id());
        assertEquals("Semester 1", response.data().name());
    }

    @Test
    void getAllSemestersShouldReturnList() {
        when(semesterService.getAllSemesters())
            .thenReturn(List.of(createSemester()));

        GenericResponse<List<SemesterResponse>> response =
            semesterController.getAllSemesters();

        assertEquals(200, response.status());
        assertEquals(1, response.data().size());
        assertEquals("2026/2027", response.data().get(0).academicYear());
    }

    @Test
    void getSemesterByIdShouldReturnSemester() {
        when(semesterService.getSemesterById(1L))
            .thenReturn(createSemester());

        GenericResponse<SemesterResponse> response =
            semesterController.getSemesterById(1L);

        assertEquals(200, response.status());
        assertEquals(1L, response.data().id());
    }

    @Test
    void updateSemesterShouldReturnUpdatedSemester() {
        Semester semester = createSemester();
        when(semesterService.updateSemester(
            eq(1L), any(SemesterRequest.class)
        )).thenReturn(semester);

        GenericResponse<SemesterResponse> response =
            semesterController.updateSemester(1L, createRequest());

        assertEquals(200, response.status());
        assertEquals("SUCCESS", response.code());
        assertEquals(Short.valueOf((short) 30), response.data().maxCreditLoad());
    }
}