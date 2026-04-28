package org.example.rhcamunda.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.example.rhcamunda.dto.DemandeRequestDto;
import org.example.rhcamunda.entity.Demande;
import org.example.rhcamunda.entity.Employe;
import org.example.rhcamunda.repository.DemandeRepository;
import org.example.rhcamunda.service.DemandeService;
import org.example.rhcamunda.util.JwtUtil;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@RestController
@RequestMapping("/api/demandes")
@RequiredArgsConstructor
@Slf4j
public class DemandeController {

    private final TaskService taskService;
    private final DemandeRepository demandeRepository;
    private final DemandeService demandeService;
    private final JwtUtil jwtUtil;

    /**
     * 1️⃣ SOUMETTRE UNE DEMANDE ADMINISTRATIVE
     */
    @PostMapping("/soumettre")
    @PreAuthorize("hasAnyRole('EMPLOYE', 'CHEF_HIERARCHIQUE', 'RH', 'ADMIN')")
    public ResponseEntity<?> soumettreDemande(
            @RequestBody DemandeRequestDto request,
            @AuthenticationPrincipal Jwt jwt) {

        try {
            // ✅ Extraire le matricule dynamiquement depuis le JWT
            String employeMatricule = jwtUtil.extractMatricule(jwt);

            // ✅ Appel au service
            Map<String, Object> result = demandeService.soumettreDemande(request, employeMatricule);
            return ResponseEntity.ok(result);

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
    @PreAuthorize("hasAnyRole('EMPLOYE', 'CHEF_HIERARCHIQUE', 'RH', 'ADMIN')")
    public ResponseEntity<?> getHistorique(@AuthenticationPrincipal Jwt jwt) {
        String matricule = jwtUtil.extractMatricule(jwt);
        // ✅ Utilisation de la méthode existante dans le Repository
        List<Demande> demandes = demandeRepository.findByEmployeMatricule(matricule);
        return ResponseEntity.ok(demandes);
    }

    /**
     * 2️⃣b HISTORIQUE PAGINÉ (Angular Material compatible)
     */
    @GetMapping("/historique/page")
    @PreAuthorize("hasAnyRole('EMPLOYE', 'CHEF_HIERARCHIQUE', 'RH', 'ADMIN')")
    public ResponseEntity<Page<Demande>> getHistoriquePagine(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "dateCreation") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        String matricule = jwtUtil.extractMatricule(jwt);
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(demandeService.getDemandesByEmployeMatriculePaginees(matricule, pageable));
    }

    /**
     * LISTE PAGINÉE DE TOUTES LES DEMANDES (RH uniquement)
     */
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    public ResponseEntity<Page<Demande>> getAllDemandesPaginees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dateCreation") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String statut) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Demande> result = (statut != null && !statut.isBlank())
                ? demandeService.getDemandesByStatutPaginees(statut, pageable)
                : demandeService.getAllDemandesPaginees(pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * 3️⃣ VALIDER UNE DEMANDE (RH)
     */
    @PostMapping("/taches/{taskId}/valider")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
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
            demandeService.validerTache(taskId, decision, commentaire, rhMatricule);
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
    @PreAuthorize("hasAnyRole('EMPLOYE', 'CHEF_HIERARCHIQUE', 'RH', 'ADMIN')")
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
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
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