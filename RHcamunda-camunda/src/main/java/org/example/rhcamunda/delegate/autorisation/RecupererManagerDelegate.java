package org.example.rhcamunda.delegate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.rhcamunda.repository.EmployeRepository;
import org.springframework.stereotype.Component;

@Component("recupererManagerDelegate")
@RequiredArgsConstructor
@Slf4j
public class RecupererManagerDelegate implements JavaDelegate {

    private final EmployeRepository employeRepository;

    @Override
    public void execute(DelegateExecution execution) {
        try {
            String matriculeEmploye = (String) execution.getVariable("employeMatricule");

            log.info("🔍 Recherche du manager pour l'employé: {}", matriculeEmploye);

            // Récupérer l'employé depuis la base
            employeRepository.findByMatricule(matriculeEmploye)
                    .ifPresent(employe -> {
                        // Récupérer le manager (superieur hiérarchique)
                        if (employe.getSuperieur() != null) {
                            String managerMatricule = employe.getSuperieur().getMatricule();
                            execution.setVariable("managerId", managerMatricule);
                            log.info("✅ Manager trouvé: {} pour l'employé {}",
                                    managerMatricule, matriculeEmploye);
                        } else {
                            // Fallback: utiliser admin par défaut
                            execution.setVariable("managerId", "admin");
                            log.warn("⚠️ Aucun manager défini pour {}, utilisation de 'admin'",
                                    matriculeEmploye);
                        }
                    });

        } catch (Exception e) {
            log.error("❌ Erreur lors de la récupération du manager: {}", e.getMessage());
            // En cas d'erreur, mettre admin par défaut
            execution.setVariable("managerId", "admin");
        }
    }
}