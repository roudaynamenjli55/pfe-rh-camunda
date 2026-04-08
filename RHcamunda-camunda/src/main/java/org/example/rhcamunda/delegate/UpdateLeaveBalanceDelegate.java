package org.example.rhcamunda.delegate;

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

        // 🔹 Récupération sécurisée des variables (gestion Double/Integer/Long)
        Integer employeeId = getIntegerVariable(execution, "employeeId");
        Integer requestedDays = getIntegerVariable(execution, "requestedDays");
        Integer currentBalance = getIntegerVariable(execution, "currentBalance");

        if (employeeId == null || requestedDays == null || currentBalance == null) {
            LOGGER.error("❌ Variables manquantes pour la mise à jour du solde");
            throw new IllegalArgumentException("Variables requises manquantes: employeeId, requestedDays, currentBalance");
        }

        // 🔹 Calcul du nouveau solde
        Integer newBalance = currentBalance - requestedDays;

        // 🔹 Mise à jour des variables processus
        execution.setVariable("newBalance", newBalance);
        execution.setVariable("leaveUpdated", true);

        // 🔹 Log informatif
        LOGGER.info("✅ Mise à jour solde - Employé: {}, Ancien: {}, Demandé: {}, Nouveau: {}",
                employeeId, currentBalance, requestedDays, newBalance);
    }

    /**
     * 🔹 Méthode utilitaire dynamique pour convertir n'importe quel type numérique en Integer
     * Gère: Integer, Long, Double, Float, String, Number
     */
    @SuppressWarnings("unchecked")
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
            return ((Double) value).intValue();  // ⚠️ Attention: perte de précision
        } else if (value instanceof Float) {
            return ((Float) value).intValue();
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                LOGGER.warn("⚠️ Impossible de convertir '{}' en Integer", value);
                return null;
            }
        }

        LOGGER.warn("⚠️ Type non supporté pour la variable '{}': {}", variableName, value.getClass().getName());
        return null;
    }
}