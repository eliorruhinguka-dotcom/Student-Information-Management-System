package rw.university.sims.controller;

import rw.university.sims.dto.response.AuditLogResponse;
import rw.university.sims.dto.response.GenericResponse;
import rw.university.sims.dto.response.PageResponse;
import rw.university.sims.service.AuditQueryService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;

@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogController {

    private final AuditQueryService auditQueryService;

    public AuditLogController(AuditQueryService auditQueryService) {
        this.auditQueryService = auditQueryService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    public GenericResponse<PageResponse<AuditLogResponse>> search(
            @RequestParam(required = false) Long actorId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) String outcome,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return GenericResponse.success(
            200,
            "Audit logs retrieved successfully",
            auditQueryService.search(
                actorId, action, entityType, outcome,
                from, to, page, size
            )
        );
    }
}