package org.example.rhcamunda.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.example.rhcamunda.entity.Employe;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeDTO {

    private Long id;

    // 🔹 Généré automatiquement, donc pas de validation Required
    private String matricule;

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    private String telephone;

    private LocalDate dateNaissance;

    // 🔧 CHANGÉ : LocalDate au lieu de LocalDateTime
    private LocalDate dateEmbauche;

    private String adresse;

    private Double soldeConge;

    private Integer soldeAutorisation;

    // 🔧 CHANGÉ : "actif" au lieu de "archive"
    private Boolean actif;

    // ==================== RELATIONS (IDs + Noms pour l'affichage) ====================

    private Long departementId;
    private String departementNom;

    private Long posteId;
    private String posteTitre;

    private Long superieurId;
    private String superieurNom;

    private Long agenceId;
    private String agenceNom;

    // ==================== MÉTHODES UTILITAIRES ====================

    public String getFullName() {
        return prenom + " " + nom;
    }

    // ✅ AJOUTÉ : Méthode getNomComplet() pour compatibilité avec le controller
    public String getNomComplet() {
        return getFullName();
    }

    // ==================== CONVERSION ENTITY ↔ DTO ====================

    /**
     * Convertir Entity → DTO
     */
    public static EmployeDTO fromEntity(Employe e) {
        if (e == null) return null;

        return EmployeDTO.builder()
                .id(e.getId())
                .matricule(e.getMatricule())
                .nom(e.getNom())
                .prenom(e.getPrenom())
                .email(e.getEmail())
                .telephone(e.getTelephone())
                .dateNaissance(e.getDateNaissance())
                .dateEmbauche(e.getDateEmbauche()) // LocalDate maintenant
                .adresse(e.getAdresse())
                .soldeConge(e.getSoldeConge())
                .soldeAutorisation(e.getSoldeAutorisation())
                .actif(e.getActif()) // Au lieu de archive
                .departementId(e.getDepartement() != null ? e.getDepartement().getId() : null)
                .departementNom(e.getDepartement() != null ? e.getDepartement().getNom() : null)
                .posteId(e.getPoste() != null ? e.getPoste().getId() : null)
                .posteTitre(e.getPoste() != null ? e.getPoste().getDescription() : null)
                .superieurId(e.getChefHierarchique() != null ? e.getChefHierarchique().getId() : null)
                .superieurNom(e.getChefHierarchique() != null ? e.getChefHierarchique().getFullName() : null)
                .agenceId(e.getAgence() != null ? e.getAgence().getId() : null)
                .agenceNom(e.getAgence() != null ? e.getAgence().getNom() : null)
                .build();
    }

    /**
     * Convertir DTO → Entity
     */
    public Employe toEntity() {
        return Employe.builder()
                .id(this.id)
                .matricule(this.matricule) // Sera ignoré par le service (généré auto)
                .nom(this.nom)
                .prenom(this.prenom)
                .email(this.email)
                .telephone(this.telephone)
                .dateNaissance(this.dateNaissance)
                .dateEmbauche(this.dateEmbauche)
                .adresse(this.adresse)
                .soldeConge(this.soldeConge)
                .soldeAutorisation(this.soldeAutorisation)
                .actif(this.actif != null ? this.actif : true) // Default true
                .build();
    }
}