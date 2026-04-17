package org.example.rhcamunda.delegate.evaluation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.rhcamunda.entity.Evaluation;
import org.example.rhcamunda.repository.EvaluationRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ArchiverEvaluationDelegate implements JavaDelegate {

    private final EvaluationRepository evaluationRepository;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("=== Archivage évaluation ===");

        Long evaluationId = (Long) execution.getVariable("evaluationId");

        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Évaluation non trouvée"));

        log.info("Évaluation archivée: ID={}, Employé={}, Statut={}",
                evaluationId, evaluation.getEmploye().getMatricule(), evaluation.getStatut());

        // Log d'audit pourrait être ajouté ici
    }
}