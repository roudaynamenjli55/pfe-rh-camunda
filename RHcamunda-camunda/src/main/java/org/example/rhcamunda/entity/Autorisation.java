package org.example.rhcamunda.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Entity
@Table(name = "autorisations")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Autorisation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id")
    private Employe employe;

    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;

    @Column(length = 255)
    private String motif;

    @Column(length = 50)
    private String statut;

    private Double dureeHeures;
    private Boolean estPayee;

    public Double calculerDuree() {
        if (heureDebut != null && heureFin != null) {
            return (double) (heureFin.toSecondOfDay() - heureDebut.toSecondOfDay()) / 3600;
        }
        return 0.0;
    }

    public void verifierQuotaHeures() {
        // Vérification du quota
    }

    public void mettreAJourSolde() {
        // Mise à jour du solde
    }

    public List<Evaluation> obtenirEvaluations() {
        return List.of();
    }
}