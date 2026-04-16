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
            Long demandeId = (Long) execution.getVariable("demandeId");
            Demande demande = demandeRepository.findById(demandeId)
                    .orElseThrow(() -> new RuntimeException("Demande non trouvée"));

            // Générer le PDF
            String cheminPdf = pdfService.genererPDF(demande);

            // Mettre à jour la demande
            demande.setCheminPdf(cheminPdf);
            demande.setStatut("VALIDEE");
            demandeRepository.save(demande);

            execution.setVariable("cheminPdf", cheminPdf);

            log.info("✅ PDF généré avec succès: {}", cheminPdf);

        } catch (Exception e) {
            log.error("❌ Erreur génération PDF", e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }
}