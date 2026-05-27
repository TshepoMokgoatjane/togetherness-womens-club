package za.co.twc.togetherness.womens.club.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import za.co.twc.togetherness.womens.club.domain.AuditLog;
import za.co.twc.togetherness.womens.club.domain.AuditAction;
import za.co.twc.togetherness.womens.club.repository.AuditLogRepository;

import java.time.LocalDateTime;

@Service
public class AuditService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditService.class);

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(AuditAction action,
                    String entityName,
                    Long entityId,
                    String username,
                    LocalDateTime auditTime,
                    String details) {

        try {
            LOGGER.info("AUDIT: {} {} id={} by {}", action, entityName, entityId, username);

            AuditLog auditLog = new AuditLog();
            auditLog.setAction(action);
            auditLog.setEntityName(entityName);
            auditLog.setEntityId(entityId);
            auditLog.setUsername(username);
            auditLog.setTimestamp(auditTime);
            auditLog.setDetails(details);

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            LOGGER.error("Failed to save audit log: {} {} id={} by {} - {}", action, entityName, entityId, username, e.getMessage());
        }
    }
}
