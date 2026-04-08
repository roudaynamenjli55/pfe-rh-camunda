package org.example.rhcamunda.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "conges")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Conge {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String processInstanceId; // Lien Camunda

    @Column(nullable = false, length = 50)
    private String typeConge; // repos, maladie, mariage, deuil...

    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column(nullable = false)
    private LocalDate dateFin;

    @Builder.Default
    private Integer nbjours = 0;

    // ✅ CHAMP MOTIF AJOUTÉ ICI
    @Column(columnDefinition = "TEXT")
    private String motif;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutConge statut = StatutConge.EN_ATTENTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remplacant_id")
    private Employe remplacant;

    @Builder.Default
    private Integer validations = 0;

    @Builder.Default
    private Boolean pieceJointe = false;

    @CreationTimestamp
    private LocalDateTime dateCreation;

    private LocalDateTime dateValidation;

    // Enum pour le statut
    public enum StatutConge {
        EN_ATTENTE, APPROUVE, REFUSE, ANNULE
    }

    // Méthodes métier
    public void verifierDisponibilite() {
        if (employe != null && employe.getSoldeConge() < this.nbjours) {
            throw new RuntimeException("Solde de congé insuffisant");
        }
    }

    public void mettreAJourSolde() {
        if (employe != null) {
            employe.setSoldeConge(employe.getSoldeConge() - this.nbjours);
        }
    }

    public void verifierValidations() {
        if (validations < 1) {
            throw new RuntimeException("Congé non validé");
        }
    }

    public void approuver() {
        this.statut = StatutConge.APPROUVE;
        this.dateValidation = LocalDateTime.now();
        this.validations++;
    }

    public void refuser() {
        this.statut = StatutConge.REFUSE;
        this.dateValidation = LocalDateTime.now();
    }

    public void annuler() {
        this.statut = StatutConge.ANNULE;
        if (employe != null) {
            employe.setSoldeConge(employe.getSoldeConge() + this.nbjours);
        }
    }

    public Integer calculerDuree() {
        if (dateDebut != null && dateFin != null) {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(dateDebut, dateFin) + 1;
        }
        return this.nbjours;
    }
}