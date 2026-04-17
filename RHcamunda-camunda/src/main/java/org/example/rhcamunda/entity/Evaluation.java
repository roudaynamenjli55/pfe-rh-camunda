package org.example.rhcamunda.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluations")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Evaluation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campagne_id", nullable = false)
    private CampagneEvaluation campagne;

    // Auto-évaluation (par l'employé)
    private Integer scoreAutoEvaluation;
    private String commentaireAutoEvaluation;
    private Integer nbCongesPris;
    private Integer nbAutorisationsPrises;

    // Évaluation manager
    private Integer scoreManager;
    private String commentaireManager;
    private LocalDate dateEvaluationManager;

    // Évaluation RH
    private Integer scoreRH;
    private String commentaireRH;
    private LocalDate dateEvaluationRH;

    // Score final calculé
    private Double scoreFinal;

    @Column(length = 50)
    private String statut; // EN_ATTENTE, AUTO_EVALUATION_FAITE, MANAGER_EVALUATION_FAITE, VALIDEE, REJETEE

    @CreationTimestamp
    private LocalDateTime dateCreation;

    private LocalDateTime dateValidation;

    // Méthode pour l'auto-évaluation
    public void saisirAutoEvaluation(Integer score, String commentaire,
                                     Integer nbConges, Integer nbAutorisations) {
        this.scoreAutoEvaluation = score;
        this.commentaireAutoEvaluation = commentaire;
        this.nbCongesPris = nbConges;
        this.nbAutorisationsPrises = nbAutorisations;
        this.statut = "AUTO_EVALUATION_FAITE";
    }

    // Méthode pour évaluation manager
    public void evaluerParManager(Integer score, String commentaire) {
        this.scoreManager = score;
        this.commentaireManager = commentaire;
        this.dateEvaluationManager = LocalDate.now();
        this.statut = "MANAGER_EVALUATION_FAITE";
        calculerScoreFinal();
    }

    // Méthode pour validation RH
    public void validerParRH(Integer score, String commentaire) {
        this.scoreRH = score;
        this.commentaireRH = commentaire;
        this.dateEvaluationRH = LocalDate.now();
        this.statut = "VALIDEE";
        this.dateValidation = LocalDateTime.now();
        calculerScoreFinal();
    }

    // Calcul dynamique du score final
    public void calculerScoreFinal() {
        if (scoreAutoEvaluation != null && scoreManager != null) {
            // Pondération: 30% auto-évaluation, 70% manager
            this.scoreFinal = (scoreAutoEvaluation * 0.3) + (scoreManager * 0.7);
        } else if (scoreManager != null) {
            this.scoreFinal = scoreManager.doubleValue();
        }
    }

    public boolean estComplet() {
        return scoreAutoEvaluation != null && scoreManager != null;
    }
}