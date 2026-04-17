package org.example.rhcamunda.delegate.evaluation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.rhcamunda.entity.CampagneEvaluation;
import org.example.rhcamunda.entity.Employe;
import org.example.rhcamunda.entity.Evaluation;
import org.example.rhcamunda.repository.CampagneEvaluationRepository;
import org.example.rhcamunda.repository.EmployeRepository;
import org.example.rhcamunda.repository.EvaluationRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreerEvaluationDelegate implements JavaDelegate {

    private final EvaluationRepository evaluationRepository;
    private final CampagneEvaluationRepository campagneEvaluationRepository;
    private final EmployeRepository employeRepository;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("=== Début création évaluation Camunda ===");

        String matriculeEmploye = (String) execution.getVariable("matriculeEmploye");
        Long campagneId = (Long) execution.getVariable("campagneId");
        Integer scoreAuto = (Integer) execution.getVariable("scoreAuto");
        String commentaireEmploye = (String) execution.getVariable("commentaireEmploye");

        Employe employe = employeRepository.findByMatricule(matriculeEmploye)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé: " + matriculeEmploye));

        CampagneEvaluation campagne = campagneEvaluationRepository.findById(campagneId)
                .orElseThrow(() -> new RuntimeException("Campagne non trouvée: " + campagneId));

        Evaluation evaluation = new Evaluation();
        evaluation.setEmploye(employe);
        evaluation.setCampagne(campagne);
        evaluation.setScoreAutoEvaluation(scoreAuto);
        evaluation.setCommentaireAutoEvaluation(commentaireEmploye);
        evaluation.setStatut("EN_COURS_WORKFLOW");
        evaluation.setDateCreation(LocalDateTime.now());

        Evaluation saved = evaluationRepository.save(evaluation);

        execution.setVariable("evaluationId", saved.getId());

        // Manager - à implémenter selon ta logique métier
        String matriculeManager = null;
        execution.setVariable("matriculeManager", matriculeManager);

        log.info("Évaluation créée: ID={}, Matricule Manager={}",
                saved.getId(), matriculeManager);
    }
}