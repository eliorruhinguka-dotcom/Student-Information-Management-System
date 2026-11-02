package rw.university.sims.controller;

import rw.university.sims.dto.response.GenericResponse;
import rw.university.sims.dto.response.RoleResponse;
import rw.university.sims.service.UserManagementService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
public class RoleController {

    private final UserManagementService userManagementService;

    public RoleController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public GenericResponse<List<RoleResponse>> getRoles() {
        return GenericResponse.success(
            200,
            "Roles and permissions retrieved successfully",
            userManagementService.getRoles()
        );
    }
}