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
public class NotifierManagerDelegate implements JavaDelegate {

    private final EvaluationRepository evaluationRepository;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("=== Notification au manager ===");

        Long evaluationId = (Long) execution.getVariable("evaluationId");

        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Évaluation non trouvée"));

        String nomEmploye = evaluation.getEmploye().getNom() + " " + evaluation.getEmploye().getPrenom();
        String nomCampagne = evaluation.getCampagne().getNom(); // ⚠️ "getCampagne()" pas "getCampagneEvaluation()"

        log.info("NOTIFICATION: Manager doit évaluer {} pour la campagne {}", nomEmploye, nomCampagne);
        log.info("Score auto-évaluation: {}", evaluation.getScoreAutoEvaluation()); // ⚠️ Nom correct

        execution.setVariable("notificationEnvoyee", true);
    }
}