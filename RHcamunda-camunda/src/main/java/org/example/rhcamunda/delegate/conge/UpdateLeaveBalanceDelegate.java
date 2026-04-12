package org.example.rhcamunda.delegate.conge;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("updateLeaveBalanceDelegate")
public class UpdateLeaveBalanceDelegate implements JavaDelegate {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateLeaveBalanceDelegate.class);

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // Récupération et validation des variables
        String employeeId = (String) execution.getVariable("employeeId");
        Integer requestedDays = getIntegerVariable(execution, "requestedDays");
        Integer currentBalance = getIntegerVariable(execution, "currentBalance");

        // ✅ Validation des données obligatoires
        if (employeeId == null || employeeId.isEmpty()) {
            LOGGER.error("❌ employeeId manquant ou vide");
            throw new IllegalArgumentException("employeeId est requis");
        }

        if (requestedDays == null) {
            LOGGER.error("❌ requestedDays invalide pour l'employé: {}", employeeId);
            throw new IllegalArgumentException("requestedDays doit être un nombre valide");
        }

        if (currentBalance == null) {
            LOGGER.error("❌ currentBalance invalide pour l'employé: {}", employeeId);
            throw new IllegalArgumentException("currentBalance doit être un nombre valide");
        }

        // ✅ Validation métier : jours négatifs
        if (requestedDays < 0) {
            LOGGER.error("❌ Nombre de jours demandé négatif: {}", requestedDays);
            throw new IllegalArgumentException("requestedDays ne peut pas être négatif");
        }

        // ✅ Validation métier : solde insuffisant
        if (requestedDays > currentBalance) {
            LOGGER.warn("⚠️ Solde insuffisant | Employé: {} | Solde: {} | Demandé: {}",
                    employeeId, currentBalance, requestedDays);
            execution.setVariable("leaveUpdated", false);
            execution.setVariable("insufficientBalance", true);
            return; // Arrête l'exécution, le workflow peut gérer ce cas via un gateway
        }

        // ✅ Calcul du nouveau solde
        int newBalance = currentBalance - requestedDays;

        LOGGER.info("✅ Solde mis à jour | Employé: {} | Ancien: {} | Demandé: {} | Nouveau: {}",
                employeeId, currentBalance, requestedDays, newBalance);

        // ✅ Mise à jour des variables de processus
        execution.setVariable("newBalance", newBalance);
        execution.setVariable("leaveUpdated", true);
        execution.setVariable("insufficientBalance", false);
    }

    /**
     * 🔹 Méthode utilitaire pour convertir n'importe quel type numérique en Integer
     * Gère: Integer, Long, Double, Float, String, Number
     * @return Integer ou null si la conversion échoue
     */
    private Integer getIntegerVariable(DelegateExecution execution, String variableName) {
        Object value = execution.getVariable(variableName);

        if (value == null) {
            return null;
        }

        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof Double) {
            // ⚠️ Attention: perte de précision (ex: 5.9 → 5)
            return ((Double) value).intValue();
        } else if (value instanceof Float) {
            return ((Float) value).intValue();
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt(((String) value).trim());
            } catch (NumberFormatException e) {
                LOGGER.warn("⚠️ Impossible de convertir '{}' en Integer pour '{}'", value, variableName);
                return null;
            }
        }

        LOGGER.warn("⚠️ Type non supporté pour '{}': {}", variableName, value.getClass().getName());
        return null;
    }
}