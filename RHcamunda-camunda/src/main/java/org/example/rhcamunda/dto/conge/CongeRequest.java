package org.example.rhcamunda.dto.conge;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CongeRequest {

    @NotBlank(message = "Matricule employé est requis")
    private String employeeId;

    @NotBlank(message = "Type de congé est requis")
    private String leaveType; // repos, maladie, mariage, deuil...

    @NotNull(message = "Date de début est requise")
    @FutureOrPresent(message = "La date de début doit être dans le futur")
    private LocalDate startDate;

    @NotNull(message = "Date de fin est requise")
    @FutureOrPresent(message = "La date de fin doit être dans le futur")
    private LocalDate endDate;

    @NotBlank(message = "Motif est requis")
    @Size(min = 10, message = "Le motif doit contenir au moins 10 caractères")
    private String reason;

    @Min(value = 1, message = "Durée doit être au moins 1 jour")
    private Integer requestedDays;

    private String remplacantId; // Matricule du remplaçant
    private Boolean pieceJointe = false;
}