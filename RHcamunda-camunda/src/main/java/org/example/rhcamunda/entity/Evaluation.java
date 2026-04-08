package org.example.rhcamunda.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.Map;

@Entity
@Table(name = "evaluations")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Evaluation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id")
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "campagne_id")
    private CampagneEvaluation campagne;

    private Integer scoreGlobal;
    private Double scoreFinal;
    private String commentaireManager;
    private String commentaireRH;

    @Column(length = 50)
    private String statut;

    private LocalDate dateEvaluation;

    public void saisirEvaluation(Integer score, String commentaire) {
        this.scoreGlobal = score;
        this.commentaireManager = commentaire;
    }

    public void saisirEvaluationManager(Integer score, String commentaire) {
        this.scoreFinal = score.doubleValue();
        this.commentaireRH = commentaire;
    }

    public void calculerScoreFinal() {
        if (scoreGlobal != null) {
            this.scoreFinal = scoreGlobal * 0.8; // Exemple de calcul
        }
    }

    public void valider() {
        this.statut = "VALIDEE";
    }

    public void exporterFichier() {
        // Export logic
    }
}