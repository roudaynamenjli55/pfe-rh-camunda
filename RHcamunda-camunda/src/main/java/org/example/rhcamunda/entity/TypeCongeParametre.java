package org.example.rhcamunda.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "type_conge_parametres",
        indexes = {
                @Index(name = "idx_type_conge_code", columnList = "code", unique = true)
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TypeCongeParametre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Code unique du type de congé (ex: ANNUEL, MALADIE, MATERNITE, SANS_SOLDE)
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Libellé affiché (ex: "Congé Annuel")
     */
    @Column(nullable = false, length = 100)
    private String libelle;

    /**
     * Si true, le congé est déduit du solde de congés de l'employé
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean deductibleSolde = true;

    /**
     * Nombre maximum de jours autorisés par an pour ce type (null = illimité)
     */
    private Integer joursMaxParAn;

    /**
     * Si le type est actif (pour désactiver sans supprimer)
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean actif = true;
}
