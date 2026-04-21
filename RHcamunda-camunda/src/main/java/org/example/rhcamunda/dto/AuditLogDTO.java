package org.example.rhcamunda.dto;

import lombok.*;
import org.example.rhcamunda.entity.LogAction;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {

    private Long id;
    private String utilisateurMatricule;
    private String utilisateurNom;
    private String action;
    private String entite;
    private Long entiteId;
    private String ancienValeur;
    private String nouvelleValeur;
    private String details;
    private String ipAddress;
    private LocalDateTime timestamp;
    private Boolean success;
    private String errorMessage;

    // Factory method pour convertir Entity -> DTO
    public static AuditLogDTO fromEntity(LogAction log) {
        return AuditLogDTO.builder()
                .id(log.getId())
                .utilisateurMatricule(log.getUtilisateurMatricule())
                .utilisateurNom(log.getUtilisateurNom())
                .action(log.getAction().name())
                .entite(log.getEntite())
                .entiteId(log.getEntiteId())
                .ancienValeur(log.getAncienValeur())
                .nouvelleValeur(log.getNouvelleValeur())
                .details(log.getDetails())
                .ipAddress(log.getIpAddress())
                .timestamp(log.getTimestamp())
                .success(log.getSuccess())
                .errorMessage(log.getErrorMessage())
                .build();
    }
}