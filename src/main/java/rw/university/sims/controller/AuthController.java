package rw.university.sims.controller;

import jakarta.validation.Valid;

import rw.university.sims.dto.request.ChangePasswordRequest;
import rw.university.sims.dto.request.LoginRequest;
import rw.university.sims.dto.request.LogoutRequest;
import rw.university.sims.dto.request.RefreshRequest;
import rw.university.sims.dto.response.AuthResponse;
import rw.university.sims.dto.response.GenericResponse;
import rw.university.sims.service.AuthenticationService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    public AuthController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public GenericResponse<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = authenticationService.login(
            request.email(),
            request.password(),
            httpRequest.getRemoteAddr()
        );

        return GenericResponse.success(
            200,
            "Login successful",
            response
        );
    }

    @PostMapping("/refresh")
    public GenericResponse<AuthResponse> refresh(
            @Valid @RequestBody RefreshRequest request) {

        return GenericResponse.success(
            200,
            "Tokens refreshed successfully",
            authenticationService.refresh(request.refreshToken())
        );
    }

    @PostMapping("/logout")
    public GenericResponse<Void> logout(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody LogoutRequest request,
            HttpServletRequest httpRequest) {

        authenticationService.logout(
            Long.valueOf(jwt.getSubject()),
            request.refreshToken(),
            httpRequest.getRemoteAddr()
        );

        return GenericResponse.success(
            200,
            "Logout successful",
            null
        );
    }

    @PostMapping("/change-password")
    public GenericResponse<Void> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest) {

        authenticationService.changePassword(
            Long.valueOf(jwt.getSubject()),
            request,
            httpRequest.getRemoteAddr()
        );

        return GenericResponse.success(
            200,
            "Password changed successfully",
            null
        );
    }
}