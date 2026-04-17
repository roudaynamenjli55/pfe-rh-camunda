package org.example.rhcamunda.delegate.evaluation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.rhcamunda.entity.Evaluation;
import org.example.rhcamunda.repository.EvaluationRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValiderEvaluationDelegate implements JavaDelegate {

    private final EvaluationRepository evaluationRepository;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        log.info("=== Validation évaluation par manager ===");

        Long evaluationId = (Long) execution.getVariable("evaluationId");
        Boolean approuvee = (Boolean) execution.getVariable("approuvee");
        Integer scoreFinal = (Integer) execution.getVariable("scoreFinal");
        String commentaireManager = (String) execution.getVariable("commentaireManager");

        Evaluation evaluation = evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new RuntimeException("Évaluation non trouvée"));

        // ⚠️ Utilise les méthodes de l'entity
        evaluation.setScoreManager(scoreFinal);
        evaluation.setCommentaireManager(commentaireManager);
        evaluation.setDateEvaluationManager(java.time.LocalDate.now());
        evaluation.setStatut(approuvee ? "VALIDEE" : "REJETEE");

        if (approuvee) {
            // Calcul: 40% auto + 60% manager
            Integer scoreAuto = evaluation.getScoreAutoEvaluation();
            if (scoreAuto != null) {
                Double noteCalcul = (scoreAuto * 0.4) + (scoreFinal * 0.6);
                evaluation.setScoreFinal(noteCalcul);
            }
        }

        evaluation.setDateValidation(LocalDateTime.now()); // ⚠️ LocalDateTime

        evaluationRepository.save(evaluation);

        log.info("Évaluation {} - Statut: {}, Score final: {}",
                evaluationId, evaluation.getStatut(), scoreFinal);
    }
}