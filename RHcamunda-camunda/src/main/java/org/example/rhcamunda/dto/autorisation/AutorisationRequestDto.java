package org.example.rhcamunda.dto.autorisation;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class AutorisationRequestDto {

    @NonNull
    private String type; // PERSONNEL ou SERVICE

    @NonNull
    private LocalDate date;

    @NonNull
    private LocalTime heureDebut;

    @NonNull
    private LocalTime heureFin;

    private String motif;

    private String managerId; // Optionnel - sera résolu automatiquement
}