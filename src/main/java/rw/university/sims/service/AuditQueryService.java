package rw.university.sims.service;

import rw.university.sims.dto.response.AuditLogResponse;
import rw.university.sims.dto.response.PageResponse;
import rw.university.sims.entity.AuditLog;
import rw.university.sims.repository.AuditLogRepository;

import jakarta.persistence.criteria.Predicate;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditQueryService {

    private final AuditLogRepository auditLogRepository;

    public AuditQueryService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> search(
            Long actorId,
            String action,
            String entityType,
            String outcome,
            OffsetDateTime from,
            OffsetDateTime to,
            int page,
            int size) {

        if (page < 0 || size < 1 || size > 100) {
            throw new IllegalArgumentException(
                "Page must be zero or greater and size must be between 1 and 100"
            );
        }

        if (from != null && to != null && from.isAfter(to)) {
            throw new IllegalArgumentException(
                "The from date must not be after the to date"
            );
        }

        Specification<AuditLog> specification =
            (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();

                if (actorId != null) {
                    predicates.add(cb.equal(
                        root.get("actor").get("id"),
                        actorId
                    ));
                }

                if (action != null && !action.isBlank()) {
                    predicates.add(cb.equal(
                        root.get("action"),
                        action
                    ));
                }

                if (entityType != null && !entityType.isBlank()) {
                    predicates.add(cb.equal(
                        root.get("entityType"),
                        entityType
                    ));
                }

                if (outcome != null && !outcome.isBlank()) {
                    predicates.add(cb.equal(
                        root.get("outcome"),
                        outcome.toUpperCase()
                    ));
                }

                if (from != null) {
                    predicates.add(cb.greaterThanOrEqualTo(
                        root.get("occurredAt"),
                        from
                    ));
                }

                if (to != null) {
                    predicates.add(cb.lessThanOrEqualTo(
                        root.get("occurredAt"),
                        to
                    ));
                }

                return cb.and(predicates.toArray(new Predicate[0]));
            };

        Page<AuditLog> result = auditLogRepository.findAll(
            specification,
            PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "occurredAt")
            )
        );

        return new PageResponse<>(
            result.getContent().stream()
                .map(this::toResponse)
                .toList(),
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages()
        );
    }

    private AuditLogResponse toResponse(AuditLog log) {
        return new AuditLogResponse(
            log.getId(),
            log.getOccurredAt(),
            log.getActor() == null ? null : log.getActor().getId(),
            log.getAction(),
            log.getEntityType(),
            log.getEntityId(),
            log.getOldValue(),
            log.getNewValue(),
            log.getOutcome(),
            log.getIpAddress(),
            log.getCorrelationId()
        );
    }
}