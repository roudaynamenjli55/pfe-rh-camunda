package org.example.rhcamunda;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 🚀 Application principale - Gestion RH avec Camunda Workflow
 *
 * Modules activés :
 * - Gestion des employés
 * - Workflow congés/autorisations
 * - Tableau de bord RH
 * - Audit & Logging
 * - Notifications WebSocket
 *
 * Sécurité : Spring Security + JWT + Keycloak
 */
@SpringBootApplication
@EnableProcessApplication
public class RhCamundaApplication {

    public static void main(String[] args) {
        SpringApplication.run(RhCamundaApplication.class, args);

        // Message de confirmation au démarrage
        System.out.println("\n✅ ============================================");
        System.out.println("✅   RH Camunda Application - PRÊTE");
        System.out.println("✅   Port : http://localhost:8081");
        System.out.println("✅   Camunda Cockpit : /camunda");
        System.out.println("✅   API REST : /api/**");
        System.out.println("✅   WebSocket : /ws");
        System.out.println("✅ ============================================\n");
    }

    /**
     * ⚠️ NOTE IMPORTANTE :
     *
     * La méthode @PostDeploy a été SUPPRIMÉE volontairement.
     *
     * Pourquoi ?
     * - Elle créait des processus Camunda automatiquement à chaque démarrage
     * - Cela générait des demandes de congés "fantômes" dans la base
     * - Les soldes des employés étaient faussés
     *
     * Solution adoptée :
     * - Les processus sont démarrés UNIQUEMENT via l'API REST
     * - Quand un employé soumet une demande réelle : POST /api/conges
     * - Comportement prévisible et professionnel ✅
     */
}