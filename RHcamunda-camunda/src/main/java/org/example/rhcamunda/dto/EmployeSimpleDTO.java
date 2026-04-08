package org.example.rhcamunda.dto;

import lombok.*;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EmployeSimpleDTO {

    private Long id;
    private String matricule;
    private String nom;
    private String prenom;
    private String email;
    private String posteTitre;
    private String departementNom;
    private Boolean archive;
}