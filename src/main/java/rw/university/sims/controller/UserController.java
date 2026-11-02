package rw.university.sims.controller;

import jakarta.validation.Valid;

import rw.university.sims.dto.request.ResetPasswordRequest;
import rw.university.sims.dto.request.RoleAssignmentRequest;
import rw.university.sims.dto.request.UserCreateRequest;
import rw.university.sims.dto.response.GenericResponse;
import rw.university.sims.dto.response.PageResponse;
import rw.university.sims.dto.response.UserResponse;
import rw.university.sims.service.UserManagementService;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserManagementService userService;

    public UserController(UserManagementService userService) {
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public GenericResponse<UserResponse> createUser(
            @Valid @RequestBody UserCreateRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        return GenericResponse.success(
            201,
            "User account created successfully",
            userService.createUser(
                request,
                Long.valueOf(jwt.getSubject())
            )
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public GenericResponse<PageResponse<UserResponse>> searchUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Boolean enabled,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        return GenericResponse.success(
            200,
            "Users retrieved successfully",
            userService.searchUsers(
                role, departmentId, enabled,
                page, size, sortBy, direction
            )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public GenericResponse<UserResponse> getUser(@PathVariable Long id) {
        return GenericResponse.success(
            200,
            "User retrieved successfully",
            userService.getUser(id)
        );
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('ROLE_ASSIGN')")
    public GenericResponse<UserResponse> replaceRoles(
            @PathVariable Long id,
            @Valid @RequestBody RoleAssignmentRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        return GenericResponse.success(
            200,
            "User roles updated successfully",
            userService.replaceRoles(
                id,
                request,
                Long.valueOf(jwt.getSubject())
            )
        );
    }

    @PatchMapping("/{id}/disable")
    @PreAuthorize("hasAuthority('USER_DISABLE')")
    public GenericResponse<UserResponse> disableUser(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        return GenericResponse.success(
            200,
            "User disabled successfully",
            userService.disableUser(
                id,
                Long.valueOf(jwt.getSubject())
            )
        );
    }

    @PatchMapping("/{id}/enable")
    @PreAuthorize("hasAuthority('USER_DISABLE')")
    public GenericResponse<UserResponse> enableUser(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        return GenericResponse.success(
            200,
            "User enabled successfully",
            userService.enableUser(
                id,
                Long.valueOf(jwt.getSubject())
            )
        );
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('PASSWORD_RESET')")
    public GenericResponse<UserResponse> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody ResetPasswordRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        return GenericResponse.success(
            200,
            "Temporary password set successfully; user must change it",
            userService.resetPassword(
                id,
                request.temporaryPassword(),
                Long.valueOf(jwt.getSubject())
            )
        );
    }
}