package org.example.rhcamunda.delegate;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.rhcamunda.entity.Employe;
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
        // 1️⃣ Récupérer les variables du processus
        String matricule = (String) execution.getVariable("employeeId");

        // ✅ Correction: Gérer les deux types (Integer ou Double/Number)
        Object requestedDaysObj = execution.getVariable("requestedDays");
        int requestedDays = 5; // valeur par défaut

        if (requestedDaysObj instanceof Number) {
            requestedDays = ((Number) requestedDaysObj).intValue();
        }

        // 2️⃣ Chercher l'employé dans la BDD
        Employe employe = employeRepo.findByMatricule(matricule)
                .orElseThrow(() -> new RuntimeException("❌ Employé non trouvé: " + matricule));

        // 3️⃣ Vérifier le solde réel
        Double soldeActuel = employe.getSoldeConge();
        boolean balanceSufficient = (soldeActuel != null) && (soldeActuel >= requestedDays);

        // 4️⃣ Logger + Définir les variables pour le processus
        LOGGER.info("✅ Vérification solde - Matricule: {}, Demandé: {}, Solde: {}, Suffisant: {}",
                matricule, requestedDays, soldeActuel, balanceSufficient);

        execution.setVariable("balanceSufficient", balanceSufficient);
        execution.setVariable("currentBalance", soldeActuel);
        execution.setVariable("employeeName", employe.getFullName());
    }
}