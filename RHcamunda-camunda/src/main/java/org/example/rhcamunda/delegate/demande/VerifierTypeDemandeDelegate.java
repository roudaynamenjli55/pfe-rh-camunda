package org.example.rhcamunda.delegate.demande;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class VerifierTypeDemandeDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        String typeDemande = (String) execution.getVariable("typeDemande");

        log.info("🔍 Vérification du type de demande: {}", typeDemande);

        // Vérifier que le type est valide
        if (typeDemande == null || typeDemande.isEmpty()) {
            throw new RuntimeException("Type de demande requis");
        }

        execution.setVariable("typeValide", true);
    }
}