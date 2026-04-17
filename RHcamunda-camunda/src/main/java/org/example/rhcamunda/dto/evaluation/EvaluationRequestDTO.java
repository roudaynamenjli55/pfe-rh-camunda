package org.example.rhcamunda.dto.evaluation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvaluationRequestDTO {
    private Long idCampagne;
    private String matriculeEmploye;
    private Integer scoreAuto;
    private String commentaireEmploye;
    private Integer nombreCongesPris;
    private Integer nombreAutorisationsPrises;
}