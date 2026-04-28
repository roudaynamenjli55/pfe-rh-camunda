package org.example.rhcamunda.service.chatbot;

import org.example.rhcamunda.entity.Employe;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Construit le prompt système personnalisé pour le chatbot RH.
 * Ce prompt est injecté en tête de chaque conversation pour ancrer
 * l'IA dans le contexte de l'employé connecté.
 */
@Component
public class SystemPromptBuilder {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /**
     * Génère un prompt système enrichi avec les données personnelles de l'employé.
     *
     * @param employe l'employé actuellement connecté
     * @return le texte du prompt système
     */
    public String build(Employe employe) {
        StringBuilder sb = new StringBuilder();

        sb.append("Tu es un assistant RH virtuel pour l'entreprise. ");
        sb.append("Tu aides les employés à gérer leurs demandes de congés, autorisations et évaluations. ");
        sb.append("Réponds toujours en français, de manière professionnelle et bienveillante.\n\n");

        // Informations de l'employé
        sb.append("=== EMPLOYÉ CONNECTÉ ===\n");
        sb.append("Matricule     : ").append(employe.getMatricule()).append("\n");
        sb.append("Nom complet   : ").append(employe.getPrenom()).append(" ").append(employe.getNom()).append("\n");
        sb.append("Email         : ").append(employe.getEmail()).append("\n");

        if (employe.getDepartement() != null) {
            sb.append("Département   : ").append(employe.getDepartement().getNom()).append("\n");
        }
        if (employe.getPoste() != null) {
            sb.append("Poste         : ").append(employe.getPoste().getTitre()).append("\n");
        }
        if (employe.getAgence() != null) {
            sb.append("Agence        : ").append(employe.getAgence().getNom()).append("\n");
        }
        if (employe.getChefHierarchique() != null) {
            sb.append("Manager       : ").append(employe.getChefHierarchique().getPrenom())
              .append(" ").append(employe.getChefHierarchique().getNom()).append("\n");
        }
        if (employe.getDateEmbauche() != null) {
            sb.append("Date embauche : ").append(employe.getDateEmbauche().format(DATE_FMT)).append("\n");
        }

        // Soldes
        sb.append("\n=== SOLDES ACTUELS ===\n");
        sb.append("Solde congés annuels : ")
          .append(employe.getSoldeConge() != null ? employe.getSoldeConge().intValue() : 0)
          .append(" jour(s)\n");
        sb.append("Solde autorisations  : ")
          .append(employe.getSoldeAutorisation() != null ? employe.getSoldeAutorisation() : 0)
          .append(" heure(s)\n");

        // Date du jour
        sb.append("\n=== CONTEXTE ===\n");
        sb.append("Date du jour : ").append(LocalDate.now().format(DATE_FMT)).append("\n");

        // Règles de comportement
        sb.append("\n=== RÈGLES ===\n");
        sb.append("- Ne donne JAMAIS des informations concernant d'autres employés.\n");
        sb.append("- Si tu ne connais pas la réponse, dis-le honnêtement et oriente vers le service RH.\n");
        sb.append("- Pour toute action (soumettre un congé, une autorisation...), propose un bouton de redirection.\n");
        sb.append("- Utilise des listes à puces pour les informations structurées.\n");
        sb.append("- Sois concis : 3 à 5 phrases maximum par réponse.\n");

        return sb.toString();
    }
}
