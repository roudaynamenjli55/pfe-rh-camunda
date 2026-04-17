package org.example.rhcamunda.service;

import org.example.rhcamunda.repository.EmployeRepository;
import org.example.rhcamunda.entity.Employe;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EmployeeDataService {

    @Autowired
    private EmployeRepository employeRepository;

    @Autowired
    private RuntimeService runtimeService;

    // ✅ MODIFICATION: Utilisation de matricule (String) au lieu de id (Long)
    public void loadEmployeeData(String executionId, String matricule) {
        // 1. Récupérer l'employé par matricule depuis PostgreSQL
        Employe employe = employeRepository.findByMatricule(matricule)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé avec le matricule: " + matricule));

        // 2. Charger les informations de base
        runtimeService.setVariable(executionId, "employeId", employe.getId());
        runtimeService.setVariable(executionId, "employeMatricule", employe.getMatricule());
        runtimeService.setVariable(executionId, "employeNom", employe.getNom());
        runtimeService.setVariable(executionId, "employePrenom", employe.getPrenom());
        runtimeService.setVariable(executionId, "employeFullName", employe.getFullName());
        runtimeService.setVariable(executionId, "employeEmail", employe.getEmail());
        runtimeService.setVariable(executionId, "employeTelephone", employe.getTelephone());
        runtimeService.setVariable(executionId, "employeAdresse", employe.getAdresse());
        runtimeService.setVariable(executionId, "employeDateNaissance", employe.getDateNaissance());
        runtimeService.setVariable(executionId, "employeDateEmbauche", employe.getDateEmbauche());

        // 3. Charger les soldes
        runtimeService.setVariable(executionId, "soldeConge", employe.getSoldeConge());
        runtimeService.setVariable(executionId, "soldeAutorisation", employe.getSoldeAutorisation());

        // 4. Charger les relations (si elles existent)
        if (employe.getDepartement() != null) {
            runtimeService.setVariable(executionId, "departementId", employe.getDepartement().getId());
            runtimeService.setVariable(executionId, "departementNom", employe.getDepartement().getNom());
        }

        if (employe.getPoste() != null) {
            runtimeService.setVariable(executionId, "posteId", employe.getPoste().getId());
            runtimeService.setVariable(executionId, "posteTitre", employe.getPoste().getTitre());
            runtimeService.setVariable(executionId, "posteNiveau", employe.getPoste().getNiveau());
        }

        if (employe.getAgence() != null) {
            runtimeService.setVariable(executionId, "agenceId", employe.getAgence().getId());
            runtimeService.setVariable(executionId, "agenceNom", employe.getAgence().getNom());
        }

        if (employe.getChefHierarchique() != null) {
            runtimeService.setVariable(executionId, "superieurId", employe.getChefHierarchique().getId());
            runtimeService.setVariable(executionId, "superieurNom", employe.getChefHierarchique().getFullName());
            runtimeService.setVariable(executionId, "superieurEmail", employe.getChefHierarchique().getEmail());
            runtimeService.setVariable(executionId, "superieurMatricule", employe.getChefHierarchique().getMatricule());
        }

        // ✅ CORRECTION: getActif() au lieu de getArchive() + nom de variable cohérent
        runtimeService.setVariable(executionId, "estActif", employe.getActif());

        System.out.println("✅ Données employé chargées: " + employe.getFullName() +
                " (Matricule: " + employe.getMatricule() + ")");
    }

    // Méthode utilitaire pour vérifier le solde
    public boolean hasSoldeSuffisant(String matricule, int jours) {
        Employe employe = employeRepository.findByMatricule(matricule)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé: " + matricule));
        return employe.hasSoldeSuffisant(jours);
    }
}