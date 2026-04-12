package org.example.rhcamunda.dto.autorisation;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AutorisationDto {
    private Long id;
    private String matricule;
    private String nomComplet;
    private String departement;
    private String agence;
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private Double dureeHeures;
    private String statut;
    private String type;
    private String motif;
    private Boolean estPayee;
    private String processInstanceId;
}