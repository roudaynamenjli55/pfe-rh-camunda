package org.example.rhcamunda.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "campagnes_evaluation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CampagneEvaluation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nom;

    private String description;

    @Column(nullable = false)
    private LocalDate dateDebut;

    @Column(nullable = false)
    private LocalDate dateFin;

    // ✅ CHANGÉ : Integer → Long
    @Column(nullable = false)
    private Long annee;

    @Column(length = 50)
    private String statut; // ACTIVE, CLOTUREE, ANNULEE

    @CreationTimestamp
    private java.time.LocalDateTime dateCreation;

    // Relations
    @OneToMany(mappedBy = "campagne", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Evaluation> evaluations;

    // Méthodes utilitaires
    public boolean estActive() {
        return "ACTIVE".equals(this.statut);
    }

    public boolean estCloturee() {
        return "CLOTUREE".equals(this.statut);
    }

    public boolean estDansPeriode(LocalDate date) {
        return !date.isBefore(dateDebut) && !date.isAfter(dateFin);
    }
}