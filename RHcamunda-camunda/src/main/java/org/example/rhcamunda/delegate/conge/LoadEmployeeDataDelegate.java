package org.example.rhcamunda.delegate.conge;

import org.example.rhcamunda.service.EmployeeDataService;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("loadEmployeeDataDelegate")
public class LoadEmployeeDataDelegate implements JavaDelegate {

    @Autowired
    private EmployeeDataService employeeDataService;

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        // ✅ MODIFICATION: Récupérer le matricule (String) au lieu de l'ID (Long)
        String matricule = null;

        // Essayer différents noms de variables
        Object matriculeVar = execution.getVariable("matricule");
        if (matriculeVar == null) {
            matriculeVar = execution.getVariable("employeMatricule");
        }
        if (matriculeVar == null) {
            matriculeVar = execution.getVariable("employeeMatricule");
        }

        if (matriculeVar != null) {
            matricule = matriculeVar.toString();
        }

        if (matricule == null || matricule.isEmpty()) {
            throw new RuntimeException("matricule est requis pour démarrer le processus. " +
                    "Variables disponibles: " + execution.getVariableNames());
        }

        System.out.println("🔄 Chargement des données pour l'employé Matricule: " + matricule);

        // Charger les données automatiquement depuis PostgreSQL en utilisant le matricule
        employeeDataService.loadEmployeeData(execution.getId(), matricule);
    }
}