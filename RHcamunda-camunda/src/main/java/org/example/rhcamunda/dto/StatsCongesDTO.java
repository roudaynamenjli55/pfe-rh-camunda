package org.example.rhcamunda.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatsCongesDTO {
    private Integer totalCongesPris;
    private Double joursMoyensParEmploye;
    private Integer congesEnAttente;
    private Integer congesApprouves;
    private Integer congesRefuses;
}