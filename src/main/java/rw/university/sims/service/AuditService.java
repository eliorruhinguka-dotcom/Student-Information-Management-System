package rw.university.sims.service;

import rw.university.sims.entity.AuditLog;
import rw.university.sims.entity.UserAccount;
import rw.university.sims.repository.AuditLogRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(
            UserAccount actor,
            String action,
            String entityType,
            Long entityId,
            String outcome,
            String ipAddress,
            String correlationId) {

        AuditLog entry = new AuditLog(
            actor,
            action,
            entityType,
            entityId,
            outcome,
            ipAddress,
            correlationId
        );

        auditLogRepository.save(entry);
    }
}