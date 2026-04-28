package org.example.rhcamunda.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "parametres_globaux",
        indexes = {
                @Index(name = "idx_parametre_cle", columnList = "cle", unique = true)
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParametreGlobal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Clé unique du paramètre (ex: MAX_AUTORISATIONS_MOIS)
     */
    @Column(nullable = false, unique = true, length = 100)
    private String cle;

    /**
     * Valeur stockée sous forme de chaîne (convertie selon le type attendu)
     */
    @Column(nullable = false, length = 500)
    private String valeur;

    /**
     * Description lisible du paramètre
     */
    @Column(length = 500)
    private String description;

    @UpdateTimestamp
    private LocalDateTime dateModification;
}
