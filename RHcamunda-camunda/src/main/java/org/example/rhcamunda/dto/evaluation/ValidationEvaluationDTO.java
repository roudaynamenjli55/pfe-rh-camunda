package org.example.rhcamunda.dto.evaluation;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationEvaluationDTO {
    private Long evaluationId;
    private Integer scoreFinal;
    private String commentaireManager;
    private Double noteValidation;
    private boolean validee;
}