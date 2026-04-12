package org.example.rhcamunda.dto.autorisation;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ValidationRequestDto {

    @NonNull
    private String decision; // APPROUVE ou REJETE

    private String commentaire;
}