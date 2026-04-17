package org.example.rhcamunda.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.example.rhcamunda.dto.DemandeRequestDto;
import org.example.rhcamunda.entity.Demande;
import org.example.rhcamunda.entity.Employe;
import org.example.rhcamunda.repository.DemandeRepository;
import org.example.rhcamunda.repository.EmployeRepository;
import org.example.rhcamunda.util.JwtUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/demandes")
@RequiredArgsConstructor
@Slf4j
public class DemandeController {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final DemandeRepository demandeRepository;
    private final EmployeRepository employeRepository;
    private final JwtUtil jwtUtil;

    /**
     * 1️⃣ SOUMETTRE UNE DEMANDE ADMINISTRATIVE
     */
    @PostMapping("/soumettre")
    public ResponseEntity<?> soumettreDemande(
            @RequestBody DemandeRequestDto request,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            // ✅ Extraire le matricule dynamiquement depuis le JWT
            String employeMatricule = jwtUtil.extractMatricule(jwt);

            // ✅ Récupérer l'employé depuis la base de données
            Employe employe = employeRepository.findByMatricule(employeMatricule)
                    .orElseThrow(() -> new RuntimeException("Employé non trouvé: " + employeMatricule));

            // ✅ Créer la demande en base
            Demande demande = Demande.builder()
                    .employe(employe)
                    .chefHierarchique(employe.getChefHierarchique())
                    .motif(request.getMotif())
                    .statut("EN_ATTENTE")
                    .dateCreation(LocalDate.now())
                    .build();

            demande = demandeRepository.save(demande);
            log.info("✅ Demande créée en BD pour {} (ID: {})", employeMatricule, demande.getId());

            // ✅ Démarrer le workflow Camunda avec variables dynamiques
            Map<String, Object> variables = new HashMap<>();
            variables.put("demandeId", demande.getId());
            variables.put("employeMatricule", employeMatricule);
            variables.put("typeDemande", request.getType());
            variables.put("motif", request.getMotif());
            variables.put("informationsComplementaires", request.getInformationsComplementaires());

            // Récupérer le matricule RH (à adapter selon votre organisation ou laisser en dur pour test)
            variables.put("rhMatricule", "RH001");

            String processInstanceId = runtimeService.startProcessInstanceByKey(
                    "demande-administrative",
                    variables
            ).getId();

            log.info("🚀 Workflow démarré - Instance ID: {}", processInstanceId);

            return ResponseEntity.ok(Map.of(
                    "message", "✅ Demande soumise avec succès",
                    "demandeId", demande.getId(),
                    "type", request.getType(),
                    "statut", "EN_ATTENTE",
                    "processInstanceId", processInstanceId
            ));

        } catch (Exception e) {
            log.error("❌ Erreur soumission demande", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur: " + e.getMessage());
        }
    }

    /**
     * 2️⃣ HISTORIQUE DES DEMANDES (par employé)
     */
    @GetMapping("/historique")
    public ResponseEntity<?> getHistorique(@AuthenticationPrincipal Jwt jwt) {
        String matricule = jwtUtil.extractMatricule(jwt);
        // ✅ Utilisation de la méthode existante dans le Repository
        List<Demande> demandes = demandeRepository.findByEmployeMatricule(matricule);
        return ResponseEntity.ok(demandes);
    }

    /**
     * 3️⃣ VALIDER UNE DEMANDE (RH)
     */
    @PostMapping("/taches/{taskId}/valider")
    public ResponseEntity<?> validerTache(
            @PathVariable String taskId,
            @RequestParam String decision,
            @RequestParam(required = false) String commentaire,
            @AuthenticationPrincipal Jwt jwt) {

        String rhMatricule = jwtUtil.extractMatricule(jwt);

        var task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            Map<String, Object> variables = new HashMap<>();
            variables.put("decisionRH", decision);
            variables.put("commentaireRH", commentaire);
            variables.put("validatedBy", rhMatricule);
            variables.put("validationDate", LocalDateTime.now());

            taskService.complete(taskId, variables);
            log.info("✅ Demande {} par {} (Task ID: {})", decision, rhMatricule, taskId);

            return ResponseEntity.ok(Map.of(
                    "message", "✅ Décision enregistrée",
                    "taskId", taskId,
                    "decision", decision
            ));

        } catch (Exception e) {
            log.error("❌ Erreur validation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur: " + e.getMessage());
        }
    }

    /**
     * 4️⃣ TÉLÉCHARGER LE PDF GÉNÉRÉ
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<?> telechargerPDF(@PathVariable Long id) {
        try {
            Demande demande = demandeRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

            if (demande.getCheminPdf() == null || demande.getCheminPdf().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Le PDF n'est pas encore disponible");
            }

            Resource resource = new FileSystemResource(demande.getCheminPdf());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"demande_" + id + ".pdf\"")
                    .body(resource);

        } catch (Exception e) {
            log.error("❌ Erreur téléchargement PDF", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur: " + e.getMessage());
        }
    }

    /**
     * 5️⃣ LISTE DES TÂCHES EN ATTENTE (RH)
     */
    @GetMapping("/taches/en-attente")
    public ResponseEntity<?> getTachesEnAttente(@AuthenticationPrincipal Jwt jwt) {
        String rhMatricule = jwtUtil.extractMatricule(jwt);

        var tasks = taskService.createTaskQuery()
                .taskAssignee(rhMatricule)
                .orderByTaskCreateTime()
                .desc()
                .list();

        return ResponseEntity.ok(tasks.stream().map(task -> Map.of(
                "taskId", task.getId(),
                "nom", task.getName(),
                "description", task.getDescription(),
                "createTime", task.getCreateTime(),
                "processInstanceId", task.getProcessInstanceId()
        )).toList());
    }
}