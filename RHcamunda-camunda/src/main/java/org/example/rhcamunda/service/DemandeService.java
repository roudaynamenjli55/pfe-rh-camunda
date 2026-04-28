package org.example.rhcamunda.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rhcamunda.entity.Demande;
import org.example.rhcamunda.repository.DemandeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.example.rhcamunda.dto.DemandeRequestDto;
import org.example.rhcamunda.entity.Employe;
import org.example.rhcamunda.repository.EmployeRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class DemandeService {

    private final DemandeRepository demandeRepository;
    private final EmployeRepository employeRepository;
    private final RuntimeService runtimeService;
    private final TaskService taskService;

    @Transactional
    public Map<String, Object> soumettreDemande(DemandeRequestDto request, String employeMatricule) {
        Employe employe = employeRepository.findByMatricule(employeMatricule)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé: " + employeMatricule));

        Demande demande = Demande.builder()
                .employe(employe)
                .chefHierarchique(employe.getChefHierarchique())
                .type(request.getType())
                .motif(request.getMotif())
                .statut("EN_ATTENTE")
                .dateCreation(LocalDateTime.now())
                .build();

        demande = demandeRepository.save(demande);
        log.info("✅ Demande créée en BD pour {} (ID: {})", employeMatricule, demande.getId());

        Map<String, Object> variables = new HashMap<>();
        variables.put("demandeId", demande.getId());
        variables.put("employeMatricule", employeMatricule);
        variables.put("typeDemande", request.getType());
        variables.put("motif", request.getMotif());
        variables.put("informationsComplementaires", request.getInformationsComplementaires());

        variables.put("rhMatricule", "RH001");

        String processInstanceId = runtimeService.startProcessInstanceByKey(
                "demande-administrative",
                variables
        ).getId();

        demande.setProcessInstanceId(processInstanceId);
        demandeRepository.save(demande);

        log.info("🚀 Workflow démarré - Instance ID: {}", processInstanceId);

        return Map.of(
                "message", "✅ Demande soumise avec succès",
                "demandeId", demande.getId(),
                "type", request.getType(),
                "statut", "EN_ATTENTE",
                "processInstanceId", processInstanceId
        );
    }

    @Transactional
    public void validerTache(String taskId, String decision, String commentaire, String rhMatricule) {
        var task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            throw new RuntimeException("Tâche introuvable : " + taskId);
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("decisionRH", decision);
        variables.put("commentaireRH", commentaire);
        variables.put("validatedBy", rhMatricule);
        variables.put("validationDate", LocalDateTime.now());

        taskService.complete(taskId, variables);
        log.info("✅ Demande {} par {} (Task ID: {})", decision, rhMatricule, taskId);
    }

    @Transactional(readOnly = true)
    public List<Demande> getDemandesByEmployeMatricule(String matricule) {
        return demandeRepository.findByEmployeMatricule(matricule);
    }

    @Transactional(readOnly = true)
    public List<Demande> getDemandesByStatut(String statut) {
        return demandeRepository.findByStatut(statut);
    }

    @Transactional(readOnly = true)
    public List<Demande> getDemandesByPeriode(LocalDate debut, LocalDate fin) {
        return demandeRepository.findByDateCreationBetween(debut.atStartOfDay(), fin.atTime(23, 59, 59));
    }

    @Transactional(readOnly = true)
    public long countDemandesByTypeAndMonth(String type, LocalDate date) {
        LocalDateTime premierJour = date.with(java.time.temporal.TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        LocalDateTime dernierJour = date.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);

        return demandeRepository.countByTypeAndDateCreationBetween(type, premierJour, dernierJour);
    }

    // =====================================================================
    // 📄 PAGINATION (Angular Material ready)
    // =====================================================================

    @Transactional(readOnly = true)
    public Page<Demande> getDemandesByEmployeMatriculePaginees(String matricule, Pageable pageable) {
        return demandeRepository.findByEmployeMatriculePagine(matricule, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Demande> getDemandesByStatutPaginees(String statut, Pageable pageable) {
        return demandeRepository.findByStatutPagine(statut, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Demande> getAllDemandesPaginees(Pageable pageable) {
        return demandeRepository.findAllPagine(pageable);
    }
}