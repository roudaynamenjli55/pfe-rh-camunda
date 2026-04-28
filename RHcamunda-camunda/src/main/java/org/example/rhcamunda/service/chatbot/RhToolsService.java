package org.example.rhcamunda.service.chatbot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rhcamunda.entity.Employe;
import org.example.rhcamunda.repository.AutorisationRepository;
import org.example.rhcamunda.service.CongeService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;

/**
 * Service qui construit le contexte RH dynamique injecté dans le prompt IA.
 * Interroge la base de données en temps réel pour fournir des données précises.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RhToolsService {

    private final CongeService congeService;
    private final AutorisationRepository autorisationRepository;

    /**
     * Construit le bloc de contexte RH à injecter dans le prompt système.
     * Analyse le message de l'utilisateur pour déterminer quelles données sont pertinentes.
     */
    public String buildRhContext(String userMessage, Employe employe) {
        StringBuilder context = new StringBuilder();
        String msg = normalize(userMessage);

        if (containsAny(msg, "solde", "reste", "combien", "reliquat", "disponible", "jours")) {
            context.append(getSoldeContext(employe));
        }
        if (containsAny(msg, "autorisation", "permission", "sortie")) {
            context.append(getAutorisationContext(employe));
        }
        if (containsAny(msg, "type", "categorie", "quels conges", "sorte", "conge de")) {
            context.append(getTypesCongeContext());
        }
        if (containsAny(msg, "procedure", "comment", "etapes", "workflow", "delai", "process")) {
            context.append(getProcedureContext());
        }

        return context.toString();
    }

    private String getSoldeContext(Employe employe) {
        try {
            Map<String, Integer> soldes = congeService.getSoldesParType(employe.getId());
            int consomme = congeService.getCongesConsommesAnnee(
                    employe.getId(), LocalDate.now().getYear()
            );

            return """

                [DONNÉES RÉELLES — SOLDES CONGÉS DE %s]
                - Congé annuel disponible  : %d jours
                - Congé maladie disponible : %d jours
                - Autres types             : %d jours
                - Jours consommés en %d    : %d jours
                [FIN DONNÉES]

                """.formatted(
                    employe.getFullName(),
                    soldes.getOrDefault("ANNUEL",  0),
                    soldes.getOrDefault("MALADIE", 0),
                    soldes.getOrDefault("AUTRE",   0),
                    LocalDate.now().getYear(),
                    consomme
            );
        } catch (Exception e) {
            log.error("Erreur soldes pour employé {}", employe.getId(), e);
            return "\n[ERREUR: Impossible de récupérer les soldes]\n";
        }
    }

    private String getAutorisationContext(Employe employe) {
        try {
            LocalDate mois = LocalDate.now().withDayOfMonth(1);
            int maxParMois = 2; // règle métier : 2 par type par mois

            long personnel = autorisationRepository
                    .countByEmployeIdAndTypeAndMonth(employe.getId(), "PERSONNEL", mois);
            long service   = autorisationRepository
                    .countByEmployeIdAndTypeAndMonth(employe.getId(), "SERVICE", mois);

            return """

                [DONNÉES RÉELLES — AUTORISATIONS DE %s]
                - Autorisations PERSONNEL utilisées ce mois : %d / %d
                - Autorisations SERVICE utilisées ce mois   : %d / %d
                - Règle : maximum %d autorisations par type par mois
                [FIN DONNÉES]

                """.formatted(
                    employe.getFullName(),
                    personnel, maxParMois,
                    service,   maxParMois,
                    maxParMois
            );
        } catch (Exception e) {
            log.error("Erreur autorisations pour employé {}", employe.getId(), e);
            return "\n[ERREUR: Impossible de récupérer les autorisations]\n";
        }
    }

    private String getTypesCongeContext() {
        return """

            [TYPES DE CONGÉS DISPONIBLES]
            1. Congé de repos (annuel)
            2. Congé de mariage            — 3 jours ouvrables
            3. Congé de naissance          — 3 jours ouvrables
            4. Congé de décès (1er degré)  — 3 jours
            5. Congé de décès (autre)      — 1 jour
            6. Congé maladie               — certificat médical requis
            7. Changement de domicile      — 1 jour
            [FIN DONNÉES]

            """;
    }

    private String getProcedureContext() {
        return """

            [PROCÉDURES RH — WORKFLOW VALIDATION]
            Congé / Autorisation :
              1. Employé soumet la demande via l'application
              2. Notification automatique au chef hiérarchique
              3. Chef approuve ou rejette (délai : 48h)
              4. Solde mis à jour automatiquement si approuvé
              5. Notification du résultat envoyée à l'employé
            Attestation de travail / salaire :
              1. Employé sélectionne le type d'attestation
              2. Demande transmise au service RH
              3. RH génère et valide l'attestation
              4. Téléchargement PDF disponible dans l'application
            [FIN DONNÉES]

            """;
    }

    /** Normalise les accents pour une détection de mots-clés robuste */
    private String normalize(String text) {
        return text.toLowerCase()
                .replaceAll("[àáâã]", "a")
                .replaceAll("[éèêë]", "e")
                .replaceAll("[îï]",   "i")
                .replaceAll("[ôö]",   "o")
                .replaceAll("[ùûü]",  "u")
                .replaceAll("[ç]",    "c");
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}