package rw.university.sims.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import rw.university.sims.dto.request.ResultCorrectionRequest;
import rw.university.sims.dto.response.GenericResponse;
import rw.university.sims.dto.response.ResultCorrectionHistoryResponse;
import rw.university.sims.service.ResultCorrectionExtensionService;
import java.util.List;

@RestController
@RequestMapping("/api/v1/results")
public class ResultCorrectionExtensionV1Controller {
    private final ResultCorrectionExtensionService service;

    public ResultCorrectionExtensionV1Controller(ResultCorrectionExtensionService service) {
        this.service = service;
    }

    @GetMapping("/{id}/corrections")
    @PreAuthorize("hasAuthority('RESULT_READ_ALL')")
    public GenericResponse<List<ResultCorrectionHistoryResponse>> history(
            @PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return GenericResponse.success(200, "Result correction history retrieved successfully",
                service.history(id, Long.valueOf(jwt.getSubject())));
    }

    @PostMapping("/{id}/correction-requests")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('RESULT_CORRECTION_REQUEST')")
    public GenericResponse<ResultCorrectionHistoryResponse> request(
            @PathVariable Long id,
            @Valid @RequestBody ResultCorrectionRequest request,
            @AuthenticationPrincipal Jwt jwt,
            HttpServletRequest http) {
        return GenericResponse.success(201, "Correction request submitted to the Dean",
                service.request(id, request, Long.valueOf(jwt.getSubject()), http.getRemoteAddr()));
    }
}
