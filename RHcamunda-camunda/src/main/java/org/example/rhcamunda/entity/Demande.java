package org.example.rhcamunda.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "demandes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Demande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @Column(nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDate dateCreation;

    @Column(length = 50, nullable = false)
    @Builder.Default
    private String statut = "EN_ATTENTE";

    @Column(length = 100, nullable = false)
    private String motif;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chef_hierarchique_id")
    private Employe chefHierarchique;

    // ✅ CHAMPS AJOUTÉS (nécessaires pour le Controller et le PDF)
    @Column(length = 50)
    private String type; // ATTESTATION_TRAVAIL, ATTESTATION_SALAIRE, TITRE_CONGE

    private LocalDate dateValidation;
    private String raison;

    private String cheminPdf; // Pour stocker le chemin du PDF généré

    @UpdateTimestamp
    private LocalDateTime dateModification;

    // ================= MÉTHODES MÉTIER =================

    public void soumettre() {
        this.statut = "EN_ATTENTE";
        this.dateCreation = LocalDate.now();
    }

    public void verifier() {
        this.statut = "EN_COURS_DE_VERIFICATION";
    }

    public void approuver() {
        this.statut = "APPROUVEE";
        this.dateValidation = LocalDate.now();
    }

    public void refuser(String raison) {
        this.statut = "REFUSEE";
        this.raison = raison;
        this.dateValidation = LocalDate.now();
    }

    public void annuler() {
        this.statut = "ANNULEE";
        this.dateModification = LocalDateTime.now();
    }

    public String archiver() {
        this.statut = "ARCHIVEE";
        return "Archivé";
    }
}