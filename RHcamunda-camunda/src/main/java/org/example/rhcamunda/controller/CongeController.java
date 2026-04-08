package org.example.rhcamunda.controller;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.example.rhcamunda.entity.Conge;
import org.example.rhcamunda.entity.Employe;
import org.example.rhcamunda.repository.CongeRepository;
import org.example.rhcamunda.repository.EmployeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/camunda/conge")
@CrossOrigin(origins = "*")
public class CongeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CongeController.class);

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private CongeRepository congeRepository;

    @Autowired
    private EmployeRepository employeRepository;

    /**
     * Démarrer une demande de congé
     */
    @PostMapping("/start")
    public ResponseEntity<?> startCongeRequest(@RequestBody CongeRequest request) {
        try {
            // ✅ 1. Valider les données
            if (request.getEmployeeId() == null || request.getEmployeeId().isEmpty()) {
                return ResponseEntity.badRequest().body(
                        new ErrorResponse("Erreur", "Matricule employé requis")
                );
            }

            if (request.getRequestedDays() <= 0) {
                return ResponseEntity.badRequest().body(
                        new ErrorResponse("Erreur", "Nombre de jours invalide")
                );
            }

            // ✅ 2. Récupérer l'employé depuis la BD via son matricule
            String matricule = request.getEmployeeId();

            Optional<Employe> employeOpt = employeRepository.findByMatricule(matricule);
            if (!employeOpt.isPresent()) {
                LOGGER.warn("❌ Employé non trouvé avec matricule: {}", matricule);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        new ErrorResponse("Erreur", "Employé non trouvé: " + matricule)
                );
            }

            Employe employe = employeOpt.get();
            LOGGER.info("✅ Employé trouvé: {} {}", employe.getPrenom(), employe.getNom());

            // ✅ 3. Créer les variables pour Camunda
            Map<String, Object> variables = new HashMap<>();
            variables.put("employeeId", matricule);
            variables.put("leaveType", request.getLeaveType());
            variables.put("startDate", request.getStartDate());
            variables.put("endDate", request.getEndDate());
            variables.put("reason", request.getReason());
            variables.put("requestedDays", request.getRequestedDays());
            variables.put("employeeName", employe.getFullName());

            // ✅ 4. Démarrer le processus Camunda
            String processInstanceId = runtimeService.startProcessInstanceByKey(
                    "conge-request-process",
                    variables
            ).getId();

            LOGGER.info("✅ Processus démarré - Instance ID: {}, Employé: {}",
                    processInstanceId, employe.getFullName());

            // ✅ 5. Créer et sauvegarder l'entité Conge dans la BD
            Conge conge = Conge.builder()
                    .processInstanceId(processInstanceId)
                    .typeConge(request.getLeaveType())
                    .dateDebut(LocalDate.parse(request.getStartDate()))
                    .dateFin(LocalDate.parse(request.getEndDate()))
                    .nbjours(request.getRequestedDays())
                    .motif(request.getReason())
                    .employe(employe)
                    .statut(Conge.StatutConge.EN_ATTENTE)
                    .build();

            conge = congeRepository.save(conge);

            LOGGER.info("✅ Entité Conge créée - ID: {}, Process Instance: {}",
                    conge.getId(), processInstanceId);

            // ✅ 6. Retourner la réponse
            return ResponseEntity.ok(new SuccessResponse(
                    "Demande de congé soumise avec succès",
                    conge.getId(),
                    processInstanceId
            ));

        } catch (Exception e) {
            LOGGER.error("❌ Erreur lors du démarrage du processus", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("Erreur serveur", e.getMessage())
            );
        }
    }

    /**
     * Valider une demande de congé (par le chef)
     */
    @PostMapping("/{taskId}/approve")
    public ResponseEntity<?> approveLeaveRequest(
            @PathVariable String taskId,
            @RequestBody ApprovalRequest approval) {
        try {
            // Compléter la tâche avec la variable approved=true
            taskService.complete(taskId, Map.of("approved", true));

            LOGGER.info("✅ Demande approuvée - Task ID: {}", taskId);

            return ResponseEntity.ok(new SuccessResponse(
                    "Demande approuvée avec succès",
                    null,
                    null
            ));

        } catch (Exception e) {
            LOGGER.error("❌ Erreur lors de l'approbation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("Erreur", e.getMessage())
            );
        }
    }

    /**
     * Refuser une demande de congé
     */
    @PostMapping("/{taskId}/reject")
    public ResponseEntity<?> rejectLeaveRequest(
            @PathVariable String taskId,
            @RequestBody ApprovalRequest approval) {
        try {
            // Compléter la tâche avec la variable approved=false
            taskService.complete(taskId, Map.of("approved", false));

            LOGGER.info("✅ Demande rejetée - Task ID: {}", taskId);

            return ResponseEntity.ok(new SuccessResponse(
                    "Demande rejetée avec succès",
                    null,
                    null
            ));

        } catch (Exception e) {
            LOGGER.error("❌ Erreur lors du rejet", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new ErrorResponse("Erreur", e.getMessage())
            );
        }
    }

    // ==================== CLASSES INTERNES ====================

    public static class CongeRequest {
        private String employeeId;
        private String leaveType;
        private String startDate;
        private String endDate;
        private int requestedDays;
        private String reason;

        // Getters et Setters
        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

        public String getLeaveType() { return leaveType; }
        public void setLeaveType(String leaveType) { this.leaveType = leaveType; }

        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }

        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }

        public int getRequestedDays() { return requestedDays; }
        public void setRequestedDays(int requestedDays) { this.requestedDays = requestedDays; }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }

    public static class ApprovalRequest {
        private String comments;

        public String getComments() { return comments; }
        public void setComments(String comments) { this.comments = comments; }
    }

    public static class SuccessResponse {
        private String message;
        private Long congeId;
        private String processInstanceId;

        public SuccessResponse(String message, Long congeId, String processInstanceId) {
            this.message = message;
            this.congeId = congeId;
            this.processInstanceId = processInstanceId;
        }

        public String getMessage() { return message; }
        public Long getCongeId() { return congeId; }
        public String getProcessInstanceId() { return processInstanceId; }
    }

    public static class ErrorResponse {
        private String error;
        private String message;

        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
        }

        public String getError() { return error; }
        public String getMessage() { return message; }
    }
}