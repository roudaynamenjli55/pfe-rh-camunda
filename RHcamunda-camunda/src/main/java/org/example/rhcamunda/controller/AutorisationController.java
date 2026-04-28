package org.example.rhcamunda.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.example.rhcamunda.dto.autorisation.AutorisationRequestDto;
import org.example.rhcamunda.dto.autorisation.ValidationRequestDto;
import org.example.rhcamunda.service.AutorisationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/autorisations")
@RequiredArgsConstructor
@Slf4j
public class AutorisationController {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final AutorisationService autorisationService;

    // =================================================================
    // 🔹 1. SOUMETTRE UNE DEMANDE
    // =================================================================

    @PostMapping("/demander")
    @PreAuthorize("hasAnyRole('EMPLOYE', 'CHEF_HIERARCHIQUE', 'RH', 'ADMIN')")
    public ResponseEntity<?> demander(
            @RequestBody AutorisationRequestDto request,
            @AuthenticationPrincipal Jwt jwt) {

        String matricule = jwt.getClaimAsString("preferred_username");

        if (matricule == null) {
            return ResponseEntity.status(401).body("❌ Utilisateur non authentifié");
        }

        // Vérifier quota
        if (!autorisationService.quotaDisponibleParMatricule(matricule, request.getType())) {
            log.warn("⚠️ Quota dépassé pour {}: type={}", matricule, request.getType());
            return ResponseEntity.badRequest()
                    .body("❌ Quota mensuel dépassé (max 2 autorisations/" + request.getType() + ")");
        }

        // Préparer variables Camunda
        Map<String, Object> variables = new HashMap<>();
        variables.put("employeMatricule", matricule);
        variables.put("typeAutorisation", request.getType());
        variables.put("motif", request.getMotif());
        variables.put("date", request.getDate());
        variables.put("heureDebut", request.getHeureDebut());
        variables.put("heureFin", request.getHeureFin());

        // Lancer workflow
        try {
            runtimeService.startProcessInstanceByKey("workflowAutorisation", variables);
            log.info("✅ Workflow démarré pour {}: type={}, date={}",
                    matricule, request.getType(), request.getDate());

            return ResponseEntity.ok().body(Map.of(
                    "message", "✅ Demande soumise avec succès",
                    "matricule", matricule,
                    "type", request.getType(),
                    "statut", "EN_ATTENTE_VALIDATION"
            ));

        } catch (Exception e) {
            log.error("❌ Erreur lors du démarrage du workflow", e);
            return ResponseEntity.internalServerError()
                    .body("❌ Erreur serveur: " + e.getMessage());
        }
    }

    // =================================================================
    // 🔹 2. LISTER LES TÂCHES
    // =================================================================

    @GetMapping("/taches")
    @PreAuthorize("hasAnyRole('EMPLOYE', 'CHEF_HIERARCHIQUE', 'RH', 'ADMIN')")
    public ResponseEntity<?> listerTaches(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) String statut) {

        String matricule = jwt.getClaimAsString("preferred_username");
        String role = jwt.getClaimAsString("role");

        if ("MANAGER".equalsIgnoreCase(role) || "RH".equalsIgnoreCase(role)) {
            List<Task> taches = taskService.createTaskQuery()
                    .taskAssignee(matricule)
                    .orderByTaskCreateTime()
                    .desc()
                    .list();

            return ResponseEntity.ok(taches.stream().map(task -> Map.of(
                    "taskId", task.getId(),
                    "processInstanceId", task.getProcessInstanceId(),
                    "nom", task.getName(),
                    "description", task.getDescription(),
                    "createTime", task.getCreateTime()
            )).collect(Collectors.toList()));

        } else {
            return ResponseEntity.ok(autorisationService.getHistoriqueParMatricule(matricule, statut));
        }
    }

    // =================================================================
    // 🔹 3. VALIDER OU REJETER
    // =================================================================

    @PostMapping("/taches/{taskId}/valider")
    @PreAuthorize("hasAnyRole('CHEF_HIERARCHIQUE', 'RH', 'ADMIN')")
    public ResponseEntity<?> validerTache(
            @PathVariable String taskId,
            @RequestBody ValidationRequestDto validation,
            @AuthenticationPrincipal Jwt jwt) {

        String managerMatricule = jwt.getClaimAsString("preferred_username");

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        if (!managerMatricule.equals(task.getAssignee())) {
            return ResponseEntity.status(403).body("❌ Accès refusé");
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("decisionManager", validation.getDecision());
        variables.put("commentaireManager", validation.getCommentaire());
        variables.put("validatedBy", managerMatricule);
        variables.put("validationDate", java.time.LocalDateTime.now());

        taskService.complete(taskId, variables);

        log.info("✅ Tâche {} complétée par {}: décision={}",
                taskId, managerMatricule, validation.getDecision());

        return ResponseEntity.ok().body(Map.of(
                "message", "✅ Décision enregistrée",
                "taskId", taskId,
                "decision", validation.getDecision()
        ));
    }

    // =================================================================
    // 🔹 4. DÉTAILS D'UNE AUTORISATION
    // =================================================================

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYE', 'CHEF_HIERARCHIQUE', 'RH', 'ADMIN')")
    public ResponseEntity<?> getDetails(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        String matricule = jwt.getClaimAsString("preferred_username");
        String role = jwt.getClaimAsString("role");

        var aut = autorisationService.exporterAutorisation(id);

        if (!"RH".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            if (!aut.getMatricule().equals(matricule)) {
                return ResponseEntity.status(403).body("❌ Accès refusé");
            }
        }

        return ResponseEntity.ok(aut);
    }

    // =================================================================
    // 🔹 5. HISTORIQUE AVEC FILTRES
    // =================================================================

    @GetMapping("/historique")
    @PreAuthorize("hasAnyRole('EMPLOYE', 'CHEF_HIERARCHIQUE', 'RH', 'ADMIN')")
    public ResponseEntity<?> getHistorique(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) LocalDate debut,
            @RequestParam(required = false) LocalDate fin,
            @RequestParam(required = false) String statut) {

        String matricule = jwt.getClaimAsString("preferred_username");

        if (debut != null && fin != null) {
            return ResponseEntity.ok(autorisationService.getHistoriqueParPeriode(matricule, debut, fin));
        }

        return ResponseEntity.ok(autorisationService.getHistoriqueParMatricule(matricule, statut));
    }

    // =================================================================
    // 🔹 6. STATISTIQUES (RH uniquement)
    // =================================================================

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    public ResponseEntity<?> getStats(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) Integer mois,
            @RequestParam(required = false) Integer annee) {

        String role = jwt.getClaimAsString("role");

        if (!"RH".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            return ResponseEntity.status(403).body("❌ Accès réservé aux RH");
        }

        LocalDate moisRef = LocalDate.now();
        if (mois != null && annee != null) {
            moisRef = LocalDate.of(annee, mois, 1);
        }

        Map<String, Object> stats = Map.of(
                "enAttente", autorisationService.countByStatutAndMois("EN_ATTENTE", moisRef),
                "validees", autorisationService.countByStatutAndMois("VALIDEE", moisRef),
                "rejetees", autorisationService.countByStatutAndMois("REJETEE", moisRef),
                "mois", moisRef.getMonthValue(),
                "annee", moisRef.getYear()
        );

        return ResponseEntity.ok(stats);
    }
}