package org.example.rhcamunda.dto.conge;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CongeRequest {

    @NotBlank(message = "Type de congé est requis")
    private String typeConge;

    @NotNull(message = "Date de début est requise")
    @FutureOrPresent(message = "La date de début doit être aujourd'hui ou dans le futur")
    private LocalDate dateDebut;

    @NotNull(message = "Date de fin est requise")
    @FutureOrPresent(message = "La date de fin doit être aujourd'hui ou dans le futur")
    private LocalDate dateFin;

    @NotBlank(message = "Motif est requis")
    @Size(min = 10, message = "Le motif doit contenir au moins 10 caractères")
    private String motif;

    private String remplacantId;

    @Builder.Default
    private Boolean pieceJointe = false;
}