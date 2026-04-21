package org.example.rhcamunda.dto.conge;

import lombok.*;

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
    private Integer congesParType;
}