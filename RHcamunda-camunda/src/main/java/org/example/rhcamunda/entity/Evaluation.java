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
    public void evaluerParManager(Integer score, String commentaire, double poidsAuto, double poidsManager, double penaliteConge, double penaliteAuto) {
        this.scoreManager = score;
        this.commentaireManager = commentaire;
        this.dateEvaluationManager = LocalDate.now();
        this.statut = "MANAGER_EVALUATION_FAITE";
        calculerScoreFinal(poidsAuto, poidsManager, penaliteConge, penaliteAuto);
    }

    // Méthode pour validation RH
    public void validerParRH(Integer score, String commentaire, double poidsAuto, double poidsManager, double penaliteConge, double penaliteAuto) {
        this.scoreRH = score;
        this.commentaireRH = commentaire;
        this.dateEvaluationRH = LocalDate.now();
        this.statut = "VALIDEE";
        this.dateValidation = LocalDateTime.now();
        calculerScoreFinal(poidsAuto, poidsManager, penaliteConge, penaliteAuto);
    }

    // Calcul dynamique du score final
    public void calculerScoreFinal(double poidsAuto, double poidsManager, double penaliteConge, double penaliteAuto) {
        double scoreBase = 0.0;
        if (scoreAutoEvaluation != null && scoreManager != null) {
            scoreBase = (scoreAutoEvaluation * poidsAuto) + (scoreManager * poidsManager);
        } else if (scoreManager != null) {
            scoreBase = scoreManager.doubleValue();
        }
        
        double malus = 0.0;
        if (nbCongesPris != null) {
            malus += nbCongesPris * penaliteConge;
        }
        if (nbAutorisationsPrises != null) {
            malus += nbAutorisationsPrises * penaliteAuto;
        }
        
        this.scoreFinal = Math.max(0.0, scoreBase - malus);
    }

    public boolean estComplet() {
        return scoreAutoEvaluation != null && scoreManager != null;
    }
}