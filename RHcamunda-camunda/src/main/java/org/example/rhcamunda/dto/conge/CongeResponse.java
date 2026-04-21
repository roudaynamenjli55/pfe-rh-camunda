package org.example.rhcamunda.dto.conge;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CongeResponse {

    private Long id;
    private String processInstanceId;
    private String employeMatricule;
    private String employeNom;
    private String typeConge;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Integer nbjours;
    private String statut; // EN_ATTENTE, APPROUVE, REFUSE, ANNULE
    private LocalDateTime dateCreation;
    private LocalDateTime dateValidation;
    private String message;
    private String nextStep;
}