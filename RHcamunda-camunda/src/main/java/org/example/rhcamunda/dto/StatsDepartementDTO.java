package org.example.rhcamunda.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsDepartementDTO {
    private String departementNom;
    private Long nombreEmployes;
    private Double pourcentage;
}