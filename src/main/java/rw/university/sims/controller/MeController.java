package rw.university.sims.controller;

import rw.university.sims.dto.response.CurrentUserResponse;
import rw.university.sims.dto.response.GenericResponse;
import rw.university.sims.service.AuthenticationService;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/me")
public class MeController {

    private final AuthenticationService authenticationService;

    public MeController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping
    public GenericResponse<CurrentUserResponse> me(
            @AuthenticationPrincipal Jwt jwt) {

        return GenericResponse.success(
            200,
            "Current account retrieved successfully",
            authenticationService.getCurrentUser(
                Long.valueOf(jwt.getSubject())
            )
        );
    }
}