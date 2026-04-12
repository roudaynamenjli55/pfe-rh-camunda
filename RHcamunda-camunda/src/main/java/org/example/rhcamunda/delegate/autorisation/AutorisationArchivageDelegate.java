package org.example.rhcamunda.delegate.autorisation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.rhcamunda.dto.autorisation.AutorisationRequestDto;
import org.example.rhcamunda.entity.Autorisation;
import org.example.rhcamunda.service.AutorisationService;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Component("autorisationArchivageDelegate")
@RequiredArgsConstructor
@Slf4j
public class AutorisationArchivageDelegate implements JavaDelegate {

    private final AutorisationService autorisationService;

    @Override
    public void execute(DelegateExecution execution) {
        try {
            // Récupérer les variables
            String matricule = (String) execution.getVariable("employeMatricule");
            String type = (String) execution.getVariable("typeAutorisation");
            String motif = (String) execution.getVariable("motif");

            // 🔧 CORRECTION DATE: Gérer tous les formats possibles
            LocalDate date = extraireDate(execution.getVariable("date"));
            LocalTime debut = extraireHeure(execution.getVariable("heureDebut"));
            LocalTime fin = extraireHeure(execution.getVariable("heureFin"));

            log.info("📝 Archivage: matricule={}, type={}, date={}, debut={}, fin={}",
                    matricule, type, date, debut, fin);

            // Créer la demande
            AutorisationRequestDto request = AutorisationRequestDto.builder()
                    .type(type)
                    .date(date)
                    .heureDebut(debut)
                    .heureFin(fin)
                    .motif(motif)
                    .build();

            Autorisation aut = autorisationService.creerDemandeParMatricule(matricule, request);

            // Stocker l'ID pour traçabilité
            execution.setVariable("autorisationId", aut.getId());
            execution.setVariable("autorisationStatut", "VALIDEE");

            log.info("✅ Autorisation archivée avec succès: ID={}", aut.getId());

        } catch (Exception e) {
            log.error("❌ Erreur lors de l'archivage: {}", e.getMessage(), e);
            throw new RuntimeException("Erreur lors de l'archivage: " + e.getMessage(), e);
        }
    }

    // 🔧 MÉTHODE UTILITAIRE: Extraire une date (gère tous les formats)
    private LocalDate extraireDate(Object dateObject) {
        if (dateObject == null) {
            return LocalDate.now();
        }

        if (dateObject instanceof LocalDate) {
            return (LocalDate) dateObject;
        }

        if (dateObject instanceof Date) {
            return ((Date) dateObject).toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        }

        if (dateObject instanceof String) {
            String dateString = (String) dateObject;
            // Essayer différents formats
            String[] formats = {
                    "yyyy-MM-dd",
                    "yyyy-MM-dd'T'HH:mm:ss",
                    "yyyy-MM-dd'T'HH:mm:ss.SSS",
                    "dd/MM/yyyy"
            };

            for (String format : formats) {
                try {
                    return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(format));
                } catch (Exception e) {
                    // Essayer le format suivant
                }
            }

            // Si aucun format ne marche, prendre juste les 10 premiers caractères
            if (dateString.length() >= 10) {
                return LocalDate.parse(dateString.substring(0, 10));
            }
        }

        throw new IllegalArgumentException("Format de date non supporté: " + dateObject);
    }

    // 🔧 MÉTHODE UTILITAIRE: Extraire une heure (gère 09:00 et 09:00:00)
    private LocalTime extraireHeure(Object heureObject) {
        if (heureObject == null) return LocalTime.of(9, 0);

        if (heureObject instanceof LocalTime) return (LocalTime) heureObject;

        if (heureObject instanceof String) {
            String h = ((String) heureObject).trim(); // Supprime les espaces invisibles

            // Cas 1 : Format 12:00:00
            if (h.length() == 8) {
                return LocalTime.parse(h);
            }
            // Cas 2 : Format 12:00
            else if (h.length() == 5) {
                return LocalTime.parse(h + ":00"); // Ajoute :00 pour que ça marche
            }
            // Cas 3 : Nettoyage manuel (au cas où)
            else {
                String[] parts = h.split(":");
                return LocalTime.of(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]));
            }
        }
        return LocalTime.of(9, 0);
    }
}