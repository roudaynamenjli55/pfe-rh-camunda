package org.example.rhcamunda.dto.conge;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CongeDTO {

    private Long id;
    private String processInstanceId;

    // Employé
    private Long employeId;
    private String employeMatricule;
    private String employeNom;
    private String employePrenom;

    // Congé
    private String typeConge;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Integer nbjours;
    private String statut;
    private String motif;
    // Remplaçant
    private Long remplacantId;
    private String remplacantNom;

    // Validation
    private Integer validations;
    private Boolean pieceJointe;
    private LocalDateTime dateCreation;
    private LocalDateTime dateValidation;

    // Solde après congé
    private Double soldeRestant;
}