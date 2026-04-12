package org.example.rhcamunda.dto.conge;

import lombok.*;
import jakarta.validation.constraints.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CongeValidationRequest {

    @NotNull(message = "Approbation est requise")
    private Boolean approved;

    private String comments;
    private String rejectionReason;
    private String validatorId; // ID du validateur (chef/RH)
}