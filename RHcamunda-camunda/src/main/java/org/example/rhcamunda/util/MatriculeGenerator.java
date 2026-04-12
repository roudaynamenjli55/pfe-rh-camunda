package org.example.rhcamunda.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rhcamunda.repository.EmployeRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class MatriculeGenerator {

    private final EmployeRepository employeRepository;

    /**
     * Génère un matricule unique au format : EMP-YYYY-NNNN
     * Exemple : EMP-2026-0001, EMP-2026-0002, etc.
     *
     * Basé sur : Année d'embauche + Numéro séquentiel
     */
    public String genererMatricule() {
        LocalDate now = LocalDate.now();
        int annee = now.getYear();

        // Compter les employés embauchés cette année (actifs ou non)
        LocalDate debutAnnee = LocalDate.of(annee, 1, 1);
        LocalDate finAnnee = now.plusDays(1); // Inclus aujourd'hui

        long count = employeRepository.countByDateEmbaucheBetween(debutAnnee, finAnnee);

        // Numéro séquentiel sur 4 chiffres (0001, 0002, ... 9999)
        String numeroSequence = String.format("%04d", count + 1);

        String matricule = "EMP-" + annee + "-" + numeroSequence;

        log.info("🆕 Matricule généré : {} ({} employés embauchés en {})",
                matricule, count + 1, annee);

        return matricule;
    }

    /**
     * Valide le format d'un matricule (pour vérification input)
     * Format attendu : EMP-YYYY-NNNN
     */
    public boolean estFormatValide(String matricule) {
        if (matricule == null || matricule.trim().isEmpty()) {
            return false;
        }
        return matricule.matches("^EMP-\\d{4}-\\d{4}$");
    }

    /**
     * Extrait l'année d'embauche depuis un matricule
     * Exemple : "EMP-2026-0001" → 2026
     */
    public int extraireAnneeEmbauche(String matricule) {
        if (!estFormatValide(matricule)) {
            throw new IllegalArgumentException("Format de matricule invalide : " + matricule);
        }
        // EMP-2026-0001 → split par "-" → [EMP, 2026, 0001]
        return Integer.parseInt(matricule.split("-")[1]);
    }
}