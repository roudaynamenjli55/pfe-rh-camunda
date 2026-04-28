package org.example.rhcamunda.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "conges",
        indexes = {
                @Index(name = "idx_conge_employe", columnList = "employe_id"),
                @Index(name = "idx_conge_statut", columnList = "statut"),
                @Index(name = "idx_conge_dates", columnList = "date_debut, date_fin"),
                @Index(name = "idx_conge_process", columnList = "process_instance_id", unique = true)
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Conge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "process_instance_id", unique = true, nullable = false, length = 100)
    private String processInstanceId; // Lien Camunda

    // ========================================================================
    // 🔹 INFORMATIONS CONGÉ
    // ========================================================================

    @Column(nullable = false, length = 50)
    @NotBlank(message = "Le type de congé est requis")
    @Size(max = 50, message = "Le type de congé ne peut pas dépasser 50 caractères")
    private String typeConge; // repos, maladie, mariage, deuil...

    @Column(name = "date_debut", nullable = false)
    @NotNull(message = "La date de début est requise")
    @PastOrPresent(message = "La date de début ne peut pas être dans le futur lointain")
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    @NotNull(message = "La date de fin est requise")
    @AssertTrue(message = "La date de fin doit être après la date de début")
    private LocalDate dateFin;

    @Column(nullable = false)
    @Min(value = 1, message = "La durée doit être d'au moins 1 jour")
    @Max(value = 90, message = "La durée ne peut pas dépasser 90 jours")
    @Builder.Default
    private Integer nbjours = 0;

    @Column(columnDefinition = "TEXT")
    @Size(max = 500, message = "Le motif ne peut pas dépasser 500 caractères")
    private String motif;

    // ========================================================================
    // 🔹 STATUT & VALIDATION
    // ========================================================================

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatutConge statut = StatutConge.EN_ATTENTE;

    @Builder.Default
    private Integer validations = 0;

    @Builder.Default
    private Boolean pieceJointe = false;

    // ========================================================================
    // 🔹 RELATIONS
    // ========================================================================

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employe_id", nullable = false, foreignKey = @ForeignKey(name = "fk_conge_employe"))
    @NotNull(message = "L'employé est requis")
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "remplacant_id", foreignKey = @ForeignKey(name = "fk_conge_remplacant"))
    private Employe remplacant;

    // ========================================================================
    // 🔹 DATES & AUDIT
    // ========================================================================

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime dateCreation;

    @UpdateTimestamp
    private LocalDateTime dateModification;

    private LocalDateTime dateValidation;

    private LocalDateTime dateAnnulation;

    // ========================================================================
    // 🔹 ENUM STATUT
    // ========================================================================

    public enum StatutConge {
        EN_ATTENTE,
        APPROUVE,
        REFUSE,
        ANNULE
    }

    // ========================================================================
    // 🔹 MÉTHODES MÉTIER
    // ========================================================================

    /**
     * Vérifie si l'employé a assez de solde pour ce congé
     */
    public boolean soldeSuffisant() {
        return employe != null && employe.getSoldeConge() >= this.nbjours;
    }

    /**
     * Met à jour le solde de l'employé (déduction)
     */
    public void deduireSolde() {
        if (employe != null && soldeSuffisant()) {
            employe.setSoldeConge(employe.getSoldeConge() - this.nbjours);
        }
    }

    /**
     * Rembourse le solde de l'employé (annulation)
     */
    public void rembourserSolde() {
        if (employe != null) {
            employe.setSoldeConge(employe.getSoldeConge() + this.nbjours);
        }
    }

    /**
     * Approuve le congé
     */
    public void approuver() {
        if (this.statut != StatutConge.EN_ATTENTE) {
            throw new IllegalStateException("Congé déjà traité");
        }
        this.statut = StatutConge.APPROUVE;
        this.dateValidation = LocalDateTime.now();
        this.validations++;
        deduireSolde();
    }

    /**
     * Approuve le congé sans déduire le solde (ex: maladie)
     */
    public void approuverSansDeduction() {
        if (this.statut != StatutConge.EN_ATTENTE) {
            throw new IllegalStateException("Congé déjà traité");
        }
        this.statut = StatutConge.APPROUVE;
        this.dateValidation = LocalDateTime.now();
        this.validations++;
    }

    /**
     * Refuse le congé
     */
    public void refuser(String motifRefus) {
        if (this.statut != StatutConge.EN_ATTENTE) {
            throw new IllegalStateException("Congé déjà traité");
        }
        this.statut = StatutConge.REFUSE;
        this.dateValidation = LocalDateTime.now();
        this.motif = motifRefus != null ? motifRefus : this.motif;
    }

    /**
     * Annule le congé (par l'employé)
     */
    public void annuler() {
        if (this.statut == StatutConge.APPROUVE) {
            rembourserSolde();
        }
        this.statut = StatutConge.ANNULE;
        this.dateAnnulation = LocalDateTime.now();
    }

    /**
     * Calcule la durée réelle en jours
     */
    public Integer calculerDureeReelle() {
        if (dateDebut != null && dateFin != null) {
            return (int) java.time.temporal.ChronoUnit.DAYS.between(dateDebut, dateFin) + 1;
        }
        return this.nbjours;
    }

    /**
     * Vérifie si le congé chevauche une période donnée
     */
    public boolean chevauchePeriode(LocalDate debut, LocalDate fin) {
        return !this.dateFin.isBefore(debut) && !this.dateDebut.isAfter(fin);
    }

    /**
     * Vérifie si le congé est dans le passé
     */
    public boolean estDansLePasse() {
        return this.dateFin.isBefore(LocalDate.now());
    }

    /**
     * Vérifie si le congé est en cours
     */
    public boolean estEnCours() {
        LocalDate today = LocalDate.now();
        return !this.dateDebut.isAfter(today) && !this.dateFin.isBefore(today);
    }

    // ========================================================================
    // 🔹 GETTERS CALCULÉS (pour les DTOs)
    // ========================================================================

    public String getEmployeMatricule() {
        return employe != null ? employe.getMatricule() : null;
    }

    public String getEmployeNomComplet() {
        return employe != null ? employe.getNom() + " " + employe.getPrenom() : null;
    }

    public String getRemplacantMatricule() {
        return remplacant != null ? remplacant.getMatricule() : null;
    }

    public String getRemplacantNomComplet() {
        return remplacant != null ? remplacant.getNom() + " " + remplacant.getPrenom() : null;
    }

    public Double getSoldeRestantApresConge() {
        if (employe == null) return null;
        return (double) (employe.getSoldeConge() - (this.statut == StatutConge.APPROUVE ? this.nbjours : 0));
    }
}