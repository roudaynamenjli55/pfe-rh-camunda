package org.example.rhcamunda.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "autorisations")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Autorisation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private LocalTime heureDebut;

    @Column(nullable = false)
    private LocalTime heureFin;

    private Double dureeHeures;

    @Column(length = 50, nullable = false)
    private String statut; // EN_ATTENTE, VALIDEE, REJETEE

    @Column(length = 100)
    private String type; // PERSONNEL, SERVICE

    @Column(length = 255)
    private String motif;

    private Boolean estPayee = false;

    // Process instance ID pour traçabilité Camunda
    @Column(name = "process_instance_id")
    private String processInstanceId;

    // =================================================================
    // 🔹 MÉTHODES MÉTIER
    // =================================================================

    /**
     * Calcule la durée en heures entre heureDebut et heureFin
     */
    public Double calculerDuree() {
        if (heureDebut != null && heureFin != null && !heureFin.isBefore(heureDebut)) {
            long seconds = heureFin.toSecondOfDay() - heureDebut.toSecondOfDay();
            this.dureeHeures = (double) seconds / 3600;
            return this.dureeHeures;
        }
        return 0.0;
    }

    /**
     * Vérifie si le quota d'autorisations est respecté (max 2 par type/mois)
     */
    public boolean verifierQuotaAutorisations() {
        // Cette logique sera implémentée dans le service
        return true;
    }

    /**
     * Met à jour le solde d'autorisations de l'employé
     */
    public void mettreAJourSolde() {
        if (this.employe != null && "VALIDEE".equals(this.statut)) {
            // Décrémenter le solde si nécessaire
            this.employe.setSoldeAutorisation(
                    Math.max(0, this.employe.getSoldeAutorisation() - 1)
            );
        }
    }

    /**
     * Vérifie si l'autorisation peut être modifiée
     */
    public boolean peutEtreModifiee() {
        return "EN_ATTENTE".equals(this.statut);
    }

    /**
     * Vérifie si l'autorisation peut être supprimée
     */
    public boolean peutEtreSupprimee() {
        return "EN_ATTENTE".equals(this.statut);
    }
}