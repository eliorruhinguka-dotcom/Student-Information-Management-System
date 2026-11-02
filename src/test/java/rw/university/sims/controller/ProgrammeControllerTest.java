package rw.university.sims.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import rw.university.sims.entity.Department;
import rw.university.sims.entity.Programme;
import rw.university.sims.service.ProgrammeService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProgrammeControllerTest {

    @Mock
    private ProgrammeService programmeService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProgrammeController controller =
                new ProgrammeController(programmeService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    private Programme createProgramme(boolean active) {

        Department department = mock(Department.class);

        when(department.getId()).thenReturn(1L);
        when(department.getCode()).thenReturn("CS");
        when(department.getName()).thenReturn("Computer Science");

        Programme programme = mock(Programme.class);

        when(programme.getId()).thenReturn(10L);
        when(programme.getCode()).thenReturn("BSC-CS");
        when(programme.getName())
                .thenReturn("Bachelor of Computer Science");
        when(programme.getDepartment()).thenReturn(department);
        when(programme.getDurationYears()).thenReturn((short) 4);
        when(programme.isActive()).thenReturn(active);

        return programme;
    }

    @Test
    void shouldCreateProgramme() throws Exception {

        Programme programme = createProgramme(true);

        when(programmeService.createProgramme(any()))
                .thenReturn(programme);

        mockMvc.perform(post("/api/v1/programmes")
                        .contentType("application/json")
                        .content("""
                                {
                                    "code": "BSC-CS",
                                    "name": "Bachelor of Computer Science",
                                    "departmentId": 1,
                                    "durationYears": 4
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.code").value("BSC-CS"))
                .andExpect(jsonPath("$.data.name")
                        .value("Bachelor of Computer Science"))
                .andExpect(jsonPath("$.data.departmentId").value(1))
                .andExpect(jsonPath("$.data.departmentCode").value("CS"))
                .andExpect(jsonPath("$.data.durationYears").value(4))
                .andExpect(jsonPath("$.data.active").value(true));

        verify(programmeService).createProgramme(any());
    }

    @Test
    void shouldGetProgrammesByDepartment() throws Exception {

        Programme programme = createProgramme(true);

        when(programmeService.getProgrammes(1L))
                .thenReturn(List.of(programme));

        mockMvc.perform(get("/api/v1/programmes")
                        .param("departmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(10))
                .andExpect(jsonPath("$.data[0].code").value("BSC-CS"))
                .andExpect(jsonPath("$.data[0].departmentName")
                        .value("Computer Science"));

        verify(programmeService).getProgrammes(1L);
    }

    @Test
    void shouldGetAllProgrammes() throws Exception {

        Programme programme = createProgramme(true);

        when(programmeService.getProgrammes(null))
                .thenReturn(List.of(programme));

        mockMvc.perform(get("/api/v1/programmes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].code").value("BSC-CS"));

        verify(programmeService).getProgrammes(null);
    }

    @Test
    void shouldUpdateProgramme() throws Exception {

        Programme programme = createProgramme(true);

        when(programmeService.updateProgramme(
                eq(10L),
                any()))
                .thenReturn(programme);

        mockMvc.perform(put("/api/v1/programmes/10")
                        .contentType("application/json")
                        .content("""
                                {
                                    "code": "BSC-CS",
                                    "name": "Bachelor of Computer Science",
                                    "departmentId": 1,
                                    "durationYears": 4
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.code").value("BSC-CS"));

        verify(programmeService).updateProgramme(
                eq(10L),
                any());
    }

    @Test
    void shouldDeactivateProgramme() throws Exception {

        Programme programme = createProgramme(false);

        when(programmeService.deactivateProgramme(10L))
                .thenReturn(programme);

        mockMvc.perform(patch(
                        "/api/v1/programmes/10/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.active").value(false));

        verify(programmeService).deactivateProgramme(10L);
    }
}