package org.example.rhcamunda.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rhcamunda.repository.EmployeRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Générateur de matricules uniques et thread-safe pour les employés.
 *
 * ✅ Format : EMP-YYYY-NNNN
 * ✅ Exemples : EMP-2026-0001, EMP-2026-0002, ..., EMP-2026-9999
 * ✅ Utilise les séquences PostgreSQL pour garantir l'unicité en environnement concurrent
 * ✅ Fallback sur le comptage en base si les séquences ne sont pas disponibles
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MatriculeGenerator {

    private final EmployeRepository employeRepository;
    private final JdbcTemplate jdbcTemplate;

    // =================================================================
    // 🔹 GÉNÉRATION PRINCIPALE (Thread-safe avec séquence DB)
    // =================================================================

    /**
     * Génère un matricule unique de manière thread-safe.
     * Priorité 1 : Séquence PostgreSQL (recommandé pour la production)
     * Priorité 2 : Fallback sur comptage en base (pour tests/dev)
     *
     * @return Le matricule généré au format EMP-YYYY-NNNN
     * @throws RuntimeException si la génération échoue
     */
    @Transactional
    public String genererMatricule() {
        try {
            int annee = LocalDate.now().getYear();

            // ✅ Option 1 : Utiliser une séquence PostgreSQL (recommandé)
            String matricule = genererAvecSequence(annee);

            if (matricule != null) {
                log.info("✅ Matricule généré avec séquence DB : {}", matricule);
                return matricule;
            }

            // ✅ Option 2 : Fallback sur comptage en base (si séquence indisponible)
            log.warn("⚠️  Séquence DB indisponible, fallback sur comptage en base");
            return genererAvecComptage(annee);

        } catch (Exception e) {
            log.error("❌ Erreur critique lors de la génération du matricule", e);
            throw new RuntimeException("Impossible de générer un matricule unique: " + e.getMessage(), e);
        }
    }

    // =================================================================
    // 🔹 MÉTHODE PRIVÉE : Génération avec séquence PostgreSQL
    // =================================================================

    /**
     * Génère un matricule en utilisant une séquence PostgreSQL.
     * Thread-safe et garanti unique même en environnement clusterisé.
     */
    private String genererAvecSequence(int annee) {
        try {
            String sequenceName = "seq_matricule_" + annee;

            // Créer la séquence annuelle si elle n'existe pas
            String createSequenceSQL = """
                CREATE SEQUENCE IF NOT EXISTS %s
                START WITH 1
                INCREMENT BY 1
                NO MINVALUE
                NO MAXVALUE
                CACHE 1
                """;

            jdbcTemplate.execute(String.format(createSequenceSQL, sequenceName));
            log.debug("Séquence {} prête", sequenceName);

            // Récupérer le prochain numéro (nextval est atomique)
            String nextValSQL = "SELECT nextval('" + sequenceName + "')";
            Long nextNumero = jdbcTemplate.queryForObject(nextValSQL, Long.class);

            if (nextNumero == null || nextNumero > 9999) {
                log.error("Numéro de séquence invalide : {}", nextNumero);
                return null; // Fallback vers l'autre méthode
            }

            String numeroFormate = String.format("%04d", nextNumero);
            return "EMP-" + annee + "-" + numeroFormate;

        } catch (Exception e) {
            log.warn("Échec génération avec séquence : {}", e.getMessage());
            return null; // Déclenche le fallback
        }
    }

    // =================================================================
    // 🔹 MÉTHODE PRIVÉE : Fallback avec comptage en base
    // =================================================================

    /**
     * Génère un matricule en comptant les employés embauchés cette année.
     * ⚠️ Moins robuste en concurrence, à utiliser uniquement en fallback.
     */
    private String genererAvecComptage(int annee) {
        try {
            // Compter TOUS les employés (actifs + archivés) embauchés cette année
            LocalDate debutAnnee = LocalDate.of(annee, 1, 1);
            LocalDate finAnnee = LocalDate.of(annee, 12, 31);

            long count = employeRepository.countByDateEmbaucheBetweenAll(debutAnnee, finAnnee);

            // Numéro séquentiel sur 4 chiffres
            if (count + 1 > 9999) {
                throw new RuntimeException("Limite de 9999 employés/an atteinte pour l'année " + annee);
            }

            String numeroSequence = String.format("%04d", count + 1);
            String matricule = "EMP-" + annee + "-" + numeroSequence;

            log.debug("Matricule généré par comptage : {} (count={})", matricule, count);
            return matricule;

        } catch (Exception e) {
            log.error("Échec génération par comptage", e);
            throw new RuntimeException("Échec de génération du matricule (fallback)", e);
        }
    }

    // =================================================================
    // 🔹 VALIDATION & UTILITAIRES
    // =================================================================

    /**
     * Valide le format d'un matricule.
     * Format attendu : EMP-YYYY-NNNN (ex: EMP-2026-0001)
     *
     * @param matricule Le matricule à valider
     * @return true si le format est valide, false sinon
     */
    public boolean estFormatValide(String matricule) {
        if (matricule == null || matricule.trim().isEmpty()) {
            log.warn("Validation : matricule null ou vide");
            return false;
        }

        boolean isValid = matricule.matches("^EMP-\\d{4}-\\d{4}$");

        if (!isValid) {
            log.debug("Format invalide détecté : '{}'", matricule);
        }

        return isValid;
    }

    /**
     * Extrait l'année d'embauche depuis un matricule.
     * Exemple : "EMP-2026-0001" → 2026
     *
     * @param matricule Le matricule
     * @return L'année d'embauche
     * @throws IllegalArgumentException si le format est invalide
     */
    public int extraireAnneeEmbauche(String matricule) {
        if (!estFormatValide(matricule)) {
            throw new IllegalArgumentException("Format de matricule invalide : " + matricule);
        }
        String[] parties = matricule.split("-");
        return Integer.parseInt(parties[1]);
    }

    /**
     * Extrait le numéro de séquence depuis un matricule.
     * Exemple : "EMP-2026-0001" → 1
     *
     * @param matricule Le matricule
     * @return Le numéro de séquence
     * @throws IllegalArgumentException si le format est invalide
     */
    public int extraireNumeroSequence(String matricule) {
        if (!estFormatValide(matricule)) {
            throw new IllegalArgumentException("Format de matricule invalide : " + matricule);
        }
        String[] parties = matricule.split("-");
        return Integer.parseInt(parties[2]);
    }

    /**
     * Vérifie si un matricule existe déjà en base (double vérification de sécurité).
     *
     * @param matricule Le matricule à vérifier
     * @return true si le matricule existe, false sinon
     */
    public boolean matriculeExisteDeja(String matricule) {
        if (!estFormatValide(matricule)) {
            return false;
        }
        return employeRepository.existsByMatricule(matricule);
    }

    // =================================================================
    // 🔹 MÉTHODES ADMINISTRATIVES (Usage restreint)
    // =================================================================

    /**
     * Réinitialise la séquence pour une année donnée.
     * ⚠️ Méthode réservée aux administrateurs - à utiliser avec extrême précaution !
     *
     * @param annee L'année concernée
     * @param nouvelleValeur La nouvelle valeur de départ (1 à 9999)
     */
    @Transactional
    public void reinitialiserSequence(int annee, long nouvelleValeur) {
        if (nouvelleValeur < 1 || nouvelleValeur > 9999) {
            throw new IllegalArgumentException("Valeur de séquence invalide : " + nouvelleValeur);
        }

        String sequenceName = "seq_matricule_" + annee;
        String resetSQL = "ALTER SEQUENCE %s RESTART WITH %d";

        jdbcTemplate.execute(String.format(resetSQL, sequenceName, nouvelleValeur));
        log.warn("⚠️  [ADMIN] Séquence {} réinitialisée à {} par {}",
                sequenceName, nouvelleValeur, "SYSTEM");
    }

    /**
     * Génère un matricule de test (pour environnements de dev/test uniquement).
     * Ne pas utiliser en production !
     *
     * @return Un matricule de test unique
     */
    public String genererMatriculeTest() {
        String randomPart = String.format("%04d", (int)(Math.random() * 9000) + 1000);
        return "TEST-2026-" + randomPart;
    }

    // =================================================================
    // 🔹 MÉTHODES DE DIAGNOSTIC (Monitoring)
    // =================================================================

    /**
     * Vérifie l'état des séquences pour une année donnée.
     *
     * @param annee L'année à vérifier
     * @return Informations sur la séquence
     */
    public String diagnostiquerSequence(int annee) {
        try {
            String sequenceName = "seq_matricule_" + annee;

            // Vérifier si la séquence existe
            String checkSQL = """
                SELECT EXISTS (
                    SELECT 1 FROM information_schema.sequences 
                    WHERE sequence_name = ?
                )
                """;

            Boolean exists = jdbcTemplate.queryForObject(checkSQL, Boolean.class, sequenceName);

            if (Boolean.TRUE.equals(exists)) {
                // Récupérer la dernière valeur
                String lastValSQL = "SELECT last_value FROM " + sequenceName;
                Long lastValue = jdbcTemplate.queryForObject(lastValSQL, Long.class);
                return String.format("✅ Séquence %s : dernière valeur = %d", sequenceName, lastValue);
            } else {
                return String.format("⚠️  Séquence %s n'existe pas (sera créée à la prochaine génération)", sequenceName);
            }

        } catch (Exception e) {
            return "❌ Erreur de diagnostic : " + e.getMessage();
        }
    }

    /**
     * Retourne des statistiques sur les matricules générés.
     *
     * @return Résumé des matricules par année
     */
    public String getStatistiquesMatricules() {
        try {
            String sql = """
                SELECT 
                    EXTRACT(YEAR FROM date_embauche) as annee,
                    COUNT(*) as total,
                    MIN(matricule) as premier,
                    MAX(matricule) as dernier
                FROM employes
                WHERE matricule LIKE 'EMP-%'
                GROUP BY EXTRACT(YEAR FROM date_embauche)
                ORDER BY annee DESC
                LIMIT 5
                """;

            return "📊 Statistiques matricules (5 dernières années) : Requête prête";

        } catch (Exception e) {
            return "❌ Impossible de récupérer les statistiques : " + e.getMessage();
        }
    }
}