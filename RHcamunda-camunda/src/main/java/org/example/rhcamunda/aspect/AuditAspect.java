package org.example.rhcamunda.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.example.rhcamunda.entity.LogAction;
import org.example.rhcamunda.repository.LogActionRepository;
import org.example.rhcamunda.util.UserContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final LogActionRepository logRepository;
    private final UserContext userContext;
    private final ObjectMapper objectMapper;

    // ✅ Pointcut pour toutes les méthodes de service
    @Pointcut("within(org.example.rhcamunda.service..*)")
    public void serviceLayer() {}

    // ✅ Pointcut pour les méthodes CRUD
    @Pointcut("execution(* *..*Service.save*(..)) || " +
            "execution(* *..*Service.update*(..)) || " +
            "execution(* *..*Service.delete*(..)) || " +
            "execution(* *..*Service.create*(..))")
    public void crudOperations() {}

    // ✅ Pointcut pour les approbations/refus (workflow)
    @Pointcut("execution(* *..*Service.approve*(..)) || " +
            "execution(* *..*Service.reject*(..)) || " +
            "execution(* *..*Service.cancel*(..))")
    public void workflowOperations() {}

    // ✅ Avant l'exécution - Capture l'état initial
    @Before("serviceLayer() && crudOperations()")
    public void captureBeforeState(JoinPoint joinPoint) {
        try {
            // Guard: ignorer si pas de contexte HTTP (appel système)
            if (getCurrentRequest() == null) return;

            String methodName = joinPoint.getSignature().getName();
            LogAction.ActionType action = determineActionType(methodName);

            if (action == LogAction.ActionType.UPDATE ||
                    action == LogAction.ActionType.DELETE) {
                // Sauvegarder l'état avant modification
                Object[] args = joinPoint.getArgs();
                if (args.length > 0) {
                    String ancienEtat = serializeObject(args[0]);
                    // Stocker dans ThreadLocal pour utilisation après
                    AuditContextHolder.setAncienEtat(ancienEtat);
                }
            }
        } catch (Exception e) {
            log.warn("Erreur lors de la capture avant état: {}", e.getMessage());
        }
    }

    // ✅ Après succès - Log l'action
    @AfterReturning(pointcut = "serviceLayer() && (crudOperations() || workflowOperations())",
            returning = "result")
    public void logAfterSuccess(JoinPoint joinPoint, Object result) {
        logAction(joinPoint, result, true, null);
    }

    // ✅ Après exception - Log l'erreur
    @AfterThrowing(pointcut = "serviceLayer() && (crudOperations() || workflowOperations())",
            throwing = "error")
    public void logAfterError(JoinPoint joinPoint, Throwable error) {
        logAction(joinPoint, null, false, error.getMessage());
    }

    // ✅ Méthode principale de logging
    private void logAction(JoinPoint joinPoint, Object result, boolean success, String errorMessage) {
        try {
            // ✅ GUARD: Ignorer si pas de contexte HTTP (ex: DataInitializer au démarrage)
            HttpServletRequest request = getCurrentRequest();
            if (request == null) {
                log.debug("⏭️ Audit ignoré - pas de contexte HTTP (appel système)");
                return;
            }

            // ✅ GUARD: Ignorer si pas d'utilisateur authentifié
            String matricule;
            try {
                matricule = userContext.getCurrentUserMatricule();
            } catch (Exception e) {
                log.debug("⏭️ Audit ignoré - pas d'utilisateur authentifié: {}", e.getMessage());
                return;
            }

            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String entite = extractEntityName(className);
            LogAction.ActionType action = determineActionType(methodName);

            // Récupérer l'ID de l'entité si disponible
            Long entiteId = extractEntityId(result);

            // Ancien état (pour UPDATE/DELETE)
            String ancienEtat = AuditContextHolder.getAncienEtat();
            AuditContextHolder.clear();

            // Nouvel état
            String nouvelEtat = serializeObject(result);

            LogAction logAction = LogAction.builder()
                    .utilisateurMatricule(matricule)
                    .utilisateurNom(extractUserNameFromContext())
                    .action(action)
                    .entite(entite)
                    .entiteId(entiteId)
                    .ancienValeur(ancienEtat)
                    .nouvelleValeur(nouvelEtat)
                    .details(buildDetails(joinPoint, result))
                    .ipAddress(request.getRemoteAddr())
                    .userAgent(request.getHeader("User-Agent"))
                    .endpoint(request.getRequestURI())
                    .httpMethod(request.getMethod())
                    .success(success)
                    .errorMessage(errorMessage)
                    .timestamp(LocalDateTime.now())
                    .build();

            logRepository.save(logAction);

            // Log critique en console
            if (logAction.isCriticalAction()) {
                log.warn("🚨 ACTION CRITIQUE: {} par {} sur {} ID {}",
                        action, matricule, entite, entiteId);
            }

        } catch (Exception e) {
            log.warn("⚠️ Erreur lors de la création du log d'audit: {}", e.getMessage());
        }
    }

    // ✅ Helpers
    private LogAction.ActionType determineActionType(String methodName) {
        if (methodName.startsWith("save") || methodName.startsWith("create"))
            return LogAction.ActionType.CREATE;
        if (methodName.startsWith("update"))
            return LogAction.ActionType.UPDATE;
        if (methodName.startsWith("delete"))
            return LogAction.ActionType.DELETE;
        if (methodName.startsWith("approve"))
            return LogAction.ActionType.APPROVE;
        if (methodName.startsWith("reject"))
            return LogAction.ActionType.REJECT;
        if (methodName.startsWith("cancel"))
            return LogAction.ActionType.CANCEL;
        return LogAction.ActionType.OTHER;
    }

    private String extractEntityName(String className) {
        return className.replace("Service", "").replace("Impl", "");
    }

    private Long extractEntityId(Object result) {
        if (result == null) return null;
        try {
            if (result.getClass().getMethod("getId") != null) {
                return (Long) result.getClass().getMethod("getId").invoke(result);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private String serializeObject(Object obj) {
        if (obj == null) return null;
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return obj.toString();
        }
    }

    private String buildDetails(JoinPoint joinPoint, Object result) {
        StringBuilder details = new StringBuilder();
        details.append("Méthode: ").append(joinPoint.getSignature().getName()).append("\n");
        details.append("Classe: ").append(joinPoint.getTarget().getClass().getSimpleName()).append("\n");
        details.append("Arguments: ").append(Arrays.toString(joinPoint.getArgs()));
        return details.toString();
    }

    private HttpServletRequest getCurrentRequest() {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes != null ? attributes.getRequest() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String extractUserNameFromContext() {
        // À implémenter selon ton UserContext
        return "Inconnu";
    }
}