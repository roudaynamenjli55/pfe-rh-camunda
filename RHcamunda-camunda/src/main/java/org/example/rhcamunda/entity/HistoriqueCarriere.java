package org.example.rhcamunda.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "historique_carriere")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class HistoriqueCarriere {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id")
    private Employe employe;

    @CreationTimestamp
    private LocalDateTime dateChangement;

    @Column(length = 100)
    private String typeChangement;

    @Column(length = 50)
    private String ancienPoste;

    @Column(length = 50)
    private String nouveauPoste;

    @Column(length = 100)
    private String ancienDepartement;

    @Column(length = 100)
    private String nouveauDepartement;

    @Column(length = 100)
    private String ancienneAgence;

    @Column(length = 100)
    private String nouvelleAgence;

    @Column(length = 255)
    private String description;

    public void enregistrer() {
        // Logique d'enregistrement
    }

    public Map<String, Object> obtenirDetails() {
        return Map.of(
                "typeChangement", this.typeChangement,
                "ancienPoste", this.ancienPoste,
                "nouveauPoste", this.nouveauPoste,
                "dateChangement", this.dateChangement
        );
    }
}