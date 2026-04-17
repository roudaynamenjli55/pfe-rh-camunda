package org.example.rhcamunda.dto.evaluation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationDTO {
    private Long id;
    private String matriculeEmploye;
    private String nomEmploye;
    private Long idCampagne;
    private String nomCampagne;
    private Integer scoreAuto;
    private Integer scoreFinal;
    private String statut;
    private String commentaireEmploye;
    private String commentaireManager;
    private Double noteValidation;
}  // ✅ Vérifie que cette accolade existe