package org.example.rhcamunda.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rhcamunda.dto.AuditLogDTO;
import org.example.rhcamunda.entity.LogAction;
import org.example.rhcamunda.repository.LogActionRepository;
import org.example.rhcamunda.util.UserContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuditService {

    private final LogActionRepository logRepository;
    private final UserContext userContext;

    /**
     * ✅ Obtenir MES logs d'activité (dynamique via JWT)
     */
    public Page<AuditLogDTO> getMyAuditLogs(Pageable pageable) {
        String matricule = userContext.getCurrentUserMatricule();
        log.info("Récupération logs audit pour: {}", matricule);

        return logRepository.findByUtilisateurMatriculeOrderByTimestampDesc(matricule, pageable)
                .map(AuditLogDTO::fromEntity);
    }

    /**
     * ✅ Obtenir logs d'une entité spécifique
     */
    public List<AuditLogDTO> getEntityAuditTrail(String entite, Long entiteId) {
        return logRepository.findByEntiteAndEntiteIdOrderByTimestampDesc(entite, entiteId)
                .stream()
                .map(AuditLogDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * ✅ Obtenir logs par période (admin)
     */
    public Page<AuditLogDTO> getLogsByPeriod(LocalDateTime debut, LocalDateTime fin, Pageable pageable) {
        return logRepository.findByTimestampBetweenOrderByTimestampDesc(debut, fin, pageable)
                .map(AuditLogDTO::fromEntity);
    }

    /**
     * ✅ Obtenir logs critiques (sécurité)
     */
    public Page<AuditLogDTO> getCriticalLogs(Pageable pageable) {
        return logRepository.findCriticalActions(pageable)
                .map(AuditLogDTO::fromEntity);
    }

    /**
     * ✅ Statistiques pour dashboard (dynamique)
     */
    public Map<String, Object> getAuditStatistics() {
        LocalDateTime debutMois = LocalDateTime.now().minusMonths(1);

        // Actions par type
        List<Object[]> actionsByType = logRepository.countActionsByType(debutMois);
        Map<String, Long> actionStats = actionsByType.stream()
                .collect(Collectors.toMap(
                        r -> ((LogAction.ActionType) r[0]).name(),
                        r -> (Long) r[1]
                ));

        // Top utilisateurs
        List<Object[]> topUsers = logRepository.topActiveUsers(debutMois, Pageable.ofSize(10));
        List<Map<String, Object>> topUsersList = topUsers.stream()
                .map(r -> Map.of(
                        "matricule", r[0],
                        "nom", r[1],
                        "count", r[2]
                ))
                .collect(Collectors.toList());

        return Map.of(
                "actionsByType", actionStats,
                "topUsers", topUsersList,
                "totalLogs", logRepository.count(),
                "todayLogs", logRepository.countByTimestampAfter(LocalDateTime.now().withHour(0).withMinute(0).withSecond(0))
        );
    }

    /**
     * ✅ Audit trail complet d'un employé
     */
    public List<AuditLogDTO> getEmployeeAuditTrail(String matriculeEmploye) {
        LocalDateTime sixMoisAgo = LocalDateTime.now().minusMonths(6);

        return logRepository.findByUtilisateurMatriculeAndTimestampBetweenOrderByTimestampDesc(
                        matriculeEmploye, sixMoisAgo, LocalDateTime.now(), Pageable.unpaged())
                .stream()
                .map(AuditLogDTO::fromEntity)
                .collect(Collectors.toList());
    }
}