package org.example.rhcamunda.dto.conge;

import lombok.*;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CongeStatsDTO {

    // Stats générales
    private Long totalDemandes;
    private Long congesEnAttente;
    private Long congesApprouves;
    private Long congesRefuses;

    // Stats par type
    private Map<String, Long> congesParType;

    // Stats par période
    private Map<String, Long> congesParMois;

    // Taux d'approbation
    private Double tauxApprobation;

    // Durée moyenne
    private Double dureeMoyenneJours;
}