package rw.university.sims.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import rw.university.sims.dto.request.DepartmentRequest;
import rw.university.sims.entity.Department;
import rw.university.sims.service.DepartmentService;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class DepartmentControllerTest {

    @Mock
    private DepartmentService departmentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        DepartmentController controller =
                new DepartmentController(departmentService);

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    private Department createDepartment(boolean active) {

        Department department = mock(Department.class);

        when(department.getId()).thenReturn(1L);
        when(department.getCode()).thenReturn("CS");
        when(department.getName()).thenReturn("Computer Science");
        when(department.isActive()).thenReturn(active);

        return department;
    }

    @Test
    void shouldCreateDepartment() throws Exception {

        Department department = createDepartment(true);

        DepartmentRequest request =
                new DepartmentRequest("CS", "Computer Science");

        when(departmentService.createDepartment(request))
                .thenReturn(department);

        mockMvc.perform(post("/api/v1/departments")
                        .contentType("application/json")
                        .content("""
                                {
                                    "code": "CS",
                                    "name": "Computer Science"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.code").value("CS"))
                .andExpect(jsonPath("$.data.name")
                        .value("Computer Science"))
                .andExpect(jsonPath("$.data.active").value(true));

        verify(departmentService).createDepartment(request);
    }

    @Test
    void shouldGetAllDepartments() throws Exception {

        Department department = createDepartment(true);

        when(departmentService.getAllDepartments())
                .thenReturn(List.of(department));

        mockMvc.perform(get("/api/v1/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].code").value("CS"))
                .andExpect(jsonPath("$.data[0].active").value(true));

        verify(departmentService).getAllDepartments();
    }

    @Test
    void shouldGetDepartmentById() throws Exception {

        Department department = createDepartment(true);

        when(departmentService.getDepartmentById(1L))
                .thenReturn(department);

        mockMvc.perform(get("/api/v1/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.code").value("CS"))
                .andExpect(jsonPath("$.data.name")
                        .value("Computer Science"));

        verify(departmentService).getDepartmentById(1L);
    }

    @Test
    void shouldUpdateDepartment() throws Exception {

        Department department = createDepartment(true);

        when(departmentService.updateDepartment(
                eq(1L),
                any(DepartmentRequest.class)))
                .thenReturn(department);

        mockMvc.perform(put("/api/v1/departments/1")
                        .contentType("application/json")
                        .content("""
                                {
                                    "code": "CS",
                                    "name": "Computer Science"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.code").value("CS"))
                .andExpect(jsonPath("$.data.name")
                        .value("Computer Science"));

        verify(departmentService).updateDepartment(
                eq(1L),
                any(DepartmentRequest.class));
    }

    @Test
    void shouldDeactivateDepartment() throws Exception {

        Department department = createDepartment(false);

        when(departmentService.deactivateDepartment(1L))
                .thenReturn(department);

        mockMvc.perform(patch(
                        "/api/v1/departments/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.active").value(false));

        verify(departmentService).deactivateDepartment(1L);
    }
}