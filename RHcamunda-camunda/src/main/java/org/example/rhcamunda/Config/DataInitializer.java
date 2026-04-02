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

    @Override
    public void run(String... args) {
        // Créer les rôles s'ils n'existent pas
        createRoleIfNotExists("ADMIN", "Administrateur");
        createRoleIfNotExists("USER", "Utilisateur");
        createRoleIfNotExists("MANAGER", "Manager");
        createRoleIfNotExists("RH", "Ressources Humaines");
    }

    private void createRoleIfNotExists(String code, String libelle) {
        if (!roleRepository.existsByCode(code)) {
            Role role = new Role(code, libelle);
            roleRepository.save(role);
            System.out.println("✅ Rôle créé: " + code);
        }
    }
}