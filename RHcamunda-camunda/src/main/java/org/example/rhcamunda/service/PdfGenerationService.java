package org.example.rhcamunda.service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import org.example.rhcamunda.entity.Demande;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class PdfGenerationService {

    private static final String OUTPUT_DIR = "demandes-pdf/";

    public String genererPDF(Demande demande) {
        try {
            // Créer le dossier si n'existe pas
            new java.io.File(OUTPUT_DIR).mkdirs();

            // Nom du fichier dynamique
            String fileName = OUTPUT_DIR + "demande_" + demande.getId() + "_" +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";

            // ✅ iText 7 API
            PdfWriter writer = new PdfWriter(new FileOutputStream(fileName));
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Ajouter contenu selon le type
            if (demande.getType() != null) {
                switch (demande.getType()) {
                    case "ATTESTATION_TRAVAIL" -> ajouterAttestationTravail(document, demande);
                    case "ATTESTATION_SALAIRE" -> ajouterAttestationSalaire(document, demande);
                    case "TITRE_CONGE" -> ajouterTitreConge(document, demande);
                    default -> throw new RuntimeException("Type de demande non reconnu: " + demande.getType());
                }
            } else {
                document.add(new Paragraph("Demande Administrative"));
            }

            document.close();
            pdf.close();

            log.info("✅ PDF généré avec succès: {}", fileName);
            return fileName;

        } catch (Exception e) {
            log.error("❌ Erreur génération PDF", e);
            throw new RuntimeException("Erreur lors de la génération du PDF", e);
        }
    }

    private void ajouterAttestationTravail(Document document, Demande demande) {
        Paragraph title = new Paragraph("ATTESTATION DE TRAVAIL")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("Je soussigné(e), Responsable des Ressources Humaines,"));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Atteste que M./Mme " + demande.getEmploye().getPrenom() + " " +
                demande.getEmploye().getNom() + ","));
        document.add(new Paragraph("Matricule: " + demande.getEmploye().getMatricule()));

        // ✅ CORRECTION ICI : getTitre() au lieu de getNom()
        document.add(new Paragraph("Occupe le poste de: " +
                (demande.getEmploye().getPoste() != null ? demande.getEmploye().getPoste().getTitre() : "N/A")));

        document.add(new Paragraph("Au sein de notre établissement depuis le: " +
                demande.getEmploye().getDateEmbauche()));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Cette attestation est délivrée pour servir et valoir ce que de droit."));
    }

    private void ajouterAttestationSalaire(Document document, Demande demande) {
        Paragraph title = new Paragraph("ATTESTATION DE SALAIRE")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("Je soussigné(e) certifie que M./Mme " +
                demande.getEmploye().getPrenom() + " " + demande.getEmploye().getNom() + ","));
        document.add(new Paragraph("Perçoit un salaire mensuel net de: XXXX DT"));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Cette attestation est délivrée à la demande de l'intéressé(e)."));
    }

    private void ajouterTitreConge(Document document, Demande demande) {
        Paragraph title = new Paragraph("TITRE DE CONGÉ")
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER);
        document.add(title);
        document.add(new Paragraph("\n"));

        document.add(new Paragraph("L'employé(e): " + demande.getEmploye().getPrenom() + " " +
                demande.getEmploye().getNom()));
        document.add(new Paragraph("Matricule: " + demande.getEmploye().getMatricule()));
        document.add(new Paragraph("Est autorisé(e) à prendre un congé"));
        document.add(new Paragraph("\n"));
        document.add(new Paragraph("Motif: " + demande.getMotif()));
    }
}