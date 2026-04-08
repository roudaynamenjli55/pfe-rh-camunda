package org.example.rhcamunda.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "campagnes_evaluation")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CampagneEvaluation {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    private Integer annee;

    private LocalDate dateDebut;
    private LocalDate dateFin;

    @Column(length = 255)
    private String description;

    @Column(length = 50)
    private String statut;

    public void creer() {}
    public void demarrer() {}
    public void clôturer() {}
    public List<Evaluation> obtenirEvaluations() { return List.of(); }
    public void obtenirStatistiques() {}
    public void exporterStatistiques() {}
    public Map<String, Object> obtenirStatistiquesParScore() { return Map.of(); }
}