package org.example.rhcamunda.config;

import org.example.rhcamunda.entity.Role;
import org.example.rhcamunda.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private org.example.rhcamunda.service.ParametreGlobalService parametreGlobalService;

    @Autowired
    private org.example.rhcamunda.service.TypeCongeParametreService typeCongeParametreService;

    @Override
    public void run(String... args) {
        // Créer les rôles s'ils n'existent pas
        createRoleIfNotExists("ADMIN", "Administrateur");
        createRoleIfNotExists("USER", "Utilisateur");
        createRoleIfNotExists("MANAGER", "Manager");
        createRoleIfNotExists("RH", "Ressources Humaines");

        // Initialiser les paramètres globaux
        parametreGlobalService.setValeur("MAX_AUTORISATIONS_MOIS", "2", "Nombre maximum d'autorisations permises par mois");
        parametreGlobalService.setValeur("POIDS_AUTO_EVALUATION", "0.3", "Poids de l'auto-évaluation (30%)");
        parametreGlobalService.setValeur("POIDS_MANAGER_EVALUATION", "0.7", "Poids de l'évaluation manager (70%)");
        parametreGlobalService.setValeur("PENALITE_PAR_CONGE", "0.5", "Points déduits par jour de congé pris");
        parametreGlobalService.setValeur("PENALITE_PAR_AUTORISATION", "0.2", "Points déduits par autorisation prise");

        // Initialiser les types de congés
        typeCongeParametreService.createOrUpdateType("ANNUEL", "Congé Annuel", true, 30);
        typeCongeParametreService.createOrUpdateType("MALADIE", "Congé Maladie", false, 90);
        typeCongeParametreService.createOrUpdateType("MATERNITE", "Congé Maternité", false, 60);
        typeCongeParametreService.createOrUpdateType("SANS_SOLDE", "Congé Sans Solde", false, 365);
    }


    private void createRoleIfNotExists(String code, String libelle) {
        if (!roleRepository.existsByCode(code)) {
            Role role = new Role(code, libelle);
            roleRepository.save(role);
            System.out.println("✅ Rôle créé: " + code);
        }
    }
}