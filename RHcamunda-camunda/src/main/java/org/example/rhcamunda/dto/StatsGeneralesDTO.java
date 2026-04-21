package org.example.rhcamunda.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsGeneralesDTO {
    private Long totalEmployes;
    private Long employesActifs;
    private Long employesInactifs;
    private Double tauxAbsentéisme;
    private Double soldeCongeMoyen;
    private Long nouveauxEmployesMois;
}