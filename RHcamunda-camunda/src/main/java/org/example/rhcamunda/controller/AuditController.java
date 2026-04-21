package org.example.rhcamunda.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rhcamunda.dto.AuditLogDTO;
import org.example.rhcamunda.service.AuditService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AuditController {

    private final AuditService auditService;

    /**
     * 🔹 Obtenir MES logs (dynamique via JWT)
     */
    @GetMapping("/my-logs")
    public ResponseEntity<Page<AuditLogDTO>> getMyLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return ResponseEntity.ok(auditService.getMyAuditLogs(pageable));
    }

    /**
     * 🔹 Audit trail d'une entité
     */
    @GetMapping("/entity/{entite}/{id}")
    public ResponseEntity<List<AuditLogDTO>> getEntityAuditTrail(
            @PathVariable String entite,
            @PathVariable Long id) {

        return ResponseEntity.ok(auditService.getEntityAuditTrail(entite, id));
    }

    /**
     * 🔹 Logs par période (admin only)
     */
    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RH')")
    public ResponseEntity<Page<AuditLogDTO>> getLogsByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return ResponseEntity.ok(auditService.getLogsByPeriod(debut, fin, pageable));
    }

    /**
     * 🔹 Logs critiques (sécurité)
     */
    @GetMapping("/critical")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SECURITY')")
    public ResponseEntity<Page<AuditLogDTO>> getCriticalLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by("timestamp").descending());
        return ResponseEntity.ok(auditService.getCriticalLogs(pageable));
    }

    /**
     * 🔹 Statistiques d'audit (dashboard)
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RH')")
    public ResponseEntity<Map<String, Object>> getAuditStatistics() {
        return ResponseEntity.ok(auditService.getAuditStatistics());
    }

    /**
     * 🔹 Audit trail d'un employé spécifique (RH/Admin)
     */
    @GetMapping("/employee/{matricule}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('RH')")
    public ResponseEntity<List<AuditLogDTO>> getEmployeeAuditTrail(
            @PathVariable String matricule) {

        return ResponseEntity.ok(auditService.getEmployeeAuditTrail(matricule));
    }

    /**
     * 🔹 Export logs en JSON
     */
    @GetMapping("/export/json")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AuditLogDTO>> exportLogsJson(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime debut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fin) {

        PageRequest pageable = PageRequest.of(0, 10000, Sort.by("timestamp").descending());
        Page<AuditLogDTO> logs = auditService.getLogsByPeriod(debut, fin, pageable);

        return ResponseEntity.ok(logs.getContent());
    }
}