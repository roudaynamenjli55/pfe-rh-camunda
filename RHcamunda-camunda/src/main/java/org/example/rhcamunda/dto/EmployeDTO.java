package org.example.rhcamunda.dto;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class EmployeDTO {

    private Long id;
    private String matricule;
    private String nom;
    private String prenom;
    private String email;
    private String telephone;
    private LocalDate dateNaissance;
    private LocalDateTime dateEmbauche;
    private String adresse;
    private Double soldeConge;
    private Integer soldeAutorisation;
    private Boolean archive;

    // Relations (IDs seulement pour éviter les boucles)
    private Long departementId;
    private String departementNom;

    private Long posteId;
    private String posteTitre;

    private Long superieurId;
    private String superieurNom;

    private Long agenceId;
    private String agenceNom;

    public String getFullName() {
        return prenom + " " + nom;
    }
}