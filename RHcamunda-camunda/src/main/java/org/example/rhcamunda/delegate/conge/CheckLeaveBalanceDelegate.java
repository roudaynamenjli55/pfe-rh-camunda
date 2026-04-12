package org.example.rhcamunda.delegate.conge;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.rhcamunda.repository.EmployeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CheckLeaveBalanceDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(CheckLeaveBalanceDelegate.class);
    private final EmployeRepository employeRepo;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // ✅ Récupérer le matricule (String) - PAS employeeId!
        String matricule = (String) execution.getVariable("matricule");

        if (matricule == null || matricule.isEmpty()) {
            throw new RuntimeException("❌ Variable 'matricule' introuvable dans le processus");
        }

        // ✅ Chercher l'employé par MATRICULE (PAS par ID!)
        var employe = employeRepo.findByMatricule(matricule)
                .orElseThrow(() -> new RuntimeException("❌ Employé non trouvé avec le matricule: " + matricule));

        // ✅ Récupérer les jours demandés
        Object requestedDaysObj = execution.getVariable("leaveDays");
        int requestedDays = 5; // valeur par défaut

        if (requestedDaysObj instanceof Number) {
            requestedDays = ((Number) requestedDaysObj).intValue();
        }

        // ✅ Vérifier le solde réel
        Double soldeActuel = employe.getSoldeConge();
        boolean balanceSufficient = (soldeActuel != null) && (soldeActuel >= requestedDays);

        // ✅ Logger + Définir les variables pour le processus
        LOGGER.info("✅ Vérification solde - Matricule: {}, Demandé: {}, Solde: {}, Suffisant: {}",
                matricule, requestedDays, soldeActuel, balanceSufficient);

        execution.setVariable("balanceSufficient", balanceSufficient);
        execution.setVariable("currentBalance", soldeActuel);
        execution.setVariable("employeeName", employe.getFullName());
    }
}