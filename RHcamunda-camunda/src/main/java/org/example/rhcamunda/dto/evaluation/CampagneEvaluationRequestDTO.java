package org.example.rhcamunda.dto.evaluation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CampagneEvaluationRequestDTO {
    private String nom;
    private String description;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Long annee;  // ✅ Changé de Integer à Long
}