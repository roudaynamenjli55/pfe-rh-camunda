package org.example.rhcamunda.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la création d'une demande administrative
 * (Attestation de travail, Attestation de salaire, Titre de congé)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DemandeRequestDto {

    /**
     * Type de demande administrative
     * Ex: ATTESTATION_TRAVAIL, ATTESTATION_SALAIRE, TITRE_CONGE
     */
    private String type;

    /**
     * Motif de la demande
     */
    private String motif;

    /**
     * Informations complémentaires (optionnel)
     */
    private String informationsComplementaires;
}