package org.example.rhcamunda.delegate.autorisation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.rhcamunda.service.AutorisationService;
import org.springframework.stereotype.Component;

@Component("autorisationQuotaDelegate")
@RequiredArgsConstructor
@Slf4j
public class AutorisationQuotaDelegate implements JavaDelegate {

    private final AutorisationService autorisationService;

    @Override
    public void execute(DelegateExecution execution) {
        String matricule = (String) execution.getVariable("employeMatricule");
        String typeAutorisation = (String) execution.getVariable("typeAutorisation");

        if (matricule == null || typeAutorisation == null) {
            log.error("❌ Variables manquantes: matricule={}, type={}", matricule, typeAutorisation);
            execution.setVariable("soldeDisponible", false);
            return;
        }

        boolean quotaOK = autorisationService.quotaDisponibleParMatricule(matricule, typeAutorisation);
        execution.setVariable("soldeDisponible", quotaOK);

        log.info("✅ Quota vérifié pour {}: {} → {}", matricule, typeAutorisation, quotaOK ? "OK" : "DÉPASSÉ");
    }
}