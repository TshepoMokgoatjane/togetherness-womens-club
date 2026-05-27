package za.co.twc.togetherness.womens.club.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import za.co.twc.togetherness.womens.club.domain.AuditAction;
import za.co.twc.togetherness.womens.club.service.AuditService;

import java.time.LocalDateTime;

@Aspect
@Component
public class AuditAspect {

    private final AuditService auditService;

    public AuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @AfterReturning(
            pointcut = "execution(* za.co.twc..service.*.create*(..))",
            returning = "result"
    )
    public void logCreate(JoinPoint joinPoint, Object result) {

        String username = getCurrentUser();

        String entityName = result.getClass().getSimpleName();

        Long entityId = extractId(result);

        auditService.log(
                AuditAction.CREATED,
                entityName,
                entityId,
                username,
                LocalDateTime.now(),
                "Created via " + joinPoint.getSignature().getName()
        );
    }

    @AfterReturning(
            pointcut = "execution(* za.co.twc..service.*.approve*(..)) || " +
                    "execution(* za.co.twc..service.*.decline*(..))",
            returning = "result"
    )
    public void logUpdate(JoinPoint joinPoint, Object result) {

        String username = getCurrentUser();

        String entityName = result.getClass().getSimpleName();

        Long entityId = extractId(result);

        auditService.log(
                AuditAction.UPDATED,
                entityName,
                entityId,
                username,
                LocalDateTime.now(),
                "Updated via " + joinPoint.getSignature().getName()
        );
    }

    private String getCurrentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null) ? auth.getName() : "SYSTEM";
    }

    private Long extractId(Object entity) {
        try {
            return (Long) entity.getClass()
                    .getMethod("getId")
                    .invoke(entity);
        } catch (Exception e) {
            return null;
        }
    }

    @AfterReturning(
            pointcut = "execution(* za.co.twc..service.*.delete*(..))"
    )
    public void logDelete(JoinPoint joinPoint) {

        String username = getCurrentUser();
        String methodName = joinPoint.getSignature().getName();

        // Extract entity ID from method arguments (typically the first arg)
        Long entityId = null;
        if (joinPoint.getArgs().length > 0 && joinPoint.getArgs()[0] instanceof Long) {
            entityId = (Long) joinPoint.getArgs()[0];
        }

        // Derive entity name from method name (e.g., "softDeleteDependent" -> "Dependent")
        String entityName = methodName.replaceAll("(?i)(soft)?delete", "");
        if (entityName.isEmpty()) {
            entityName = joinPoint.getSignature().getDeclaringType().getSimpleName().replace("Service", "");
        }

        auditService.log(
                AuditAction.DELETED,
                entityName,
                entityId,
                username,
                LocalDateTime.now(),
                "Deleted via " + methodName
        );
    }
}
