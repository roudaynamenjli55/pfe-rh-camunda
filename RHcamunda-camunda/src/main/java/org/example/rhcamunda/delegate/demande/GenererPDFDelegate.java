package org.example.rhcamunda.delegate.demande;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.example.rhcamunda.entity.Demande;
import org.example.rhcamunda.repository.DemandeRepository;
import org.example.rhcamunda.service.PdfGenerationService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenererPDFDelegate implements JavaDelegate {

    private final PdfGenerationService pdfService;
    private final DemandeRepository demandeRepository;

    @Override
    public void execute(DelegateExecution execution) {
        try {
            // 1️⃣ Récupération sécurisée de demandeId
            Object demandeIdObj = execution.getVariable("demandeId");

            if (demandeIdObj == null) {
                log.error("❌ Variable 'demandeId' non trouvée. Variables: {}", execution.getVariableNames());
                throw new IllegalStateException("Variable 'demandeId' manquante");
            }

            Long demandeId;
            if (demandeIdObj instanceof Number) {
                demandeId = ((Number) demandeIdObj).longValue();
            } else if (demandeIdObj instanceof String) {
                demandeId = Long.valueOf((String) demandeIdObj);
            } else {
                throw new IllegalArgumentException("Type invalide pour demandeId: " + demandeIdObj.getClass());
            }

            log.info("🔍 Chargement demande ID: {}", demandeId);

            // 2️⃣ Chargement de la demande
            Demande demande = demandeRepository.findById(demandeId)
                    .orElseThrow(() -> {
                        log.error("❌ Demande non trouvée: {}", demandeId);
                        return new RuntimeException("Demande non trouvée: " + demandeId);
                    });

            // 3️⃣ Génération PDF
            String cheminPdf = pdfService.genererPDF(demande);
            log.info("📄 PDF généré: {}", cheminPdf);

            // 4️⃣ Mise à jour
            demande.setCheminPdf(cheminPdf);
            demande.setStatut("VALIDEE");
            demandeRepository.save(demande);

            // 5️⃣ Variables de sortie
            execution.setVariable("cheminPdf", cheminPdf);
            execution.setVariable("pdfGenere", true);

            log.info("✅ Succès pour demande {}", demandeId);

        } catch (RuntimeException e) {
            log.error("❌ Erreur métier", e);
            throw e;
        } catch (Exception e) {
            log.error("❌ Erreur technique", e);
            throw new RuntimeException("Erreur delegate: " + e.getMessage(), e);
        }
    }
}