package org.example.rhcamunda.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Entity
@Table(name = "employes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String matricule;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(unique = true, nullable = false, length = 100)
    private String email;

    @Column(length = 20)
    private String telephone;

    private LocalDate dateNaissance;
    private LocalDate dateEmbauche;
    private String adresse;

    @Builder.Default
    private Double soldeConge = 30.0;

    @Builder.Default
    private Integer soldeAutorisation = 12;

    @Builder.Default
    private Boolean actif = true;

    private LocalDateTime dateArretChivage;

    // ==================== RELATIONS ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departement_id")
    private Departement departement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poste_id")
    private Poste poste;

    // ✅ CORRECTION RADICALE : superieur → chefHierarchique
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chef_hierarchique_id")  // ← Nom colonne BDD
    private Employe chefHierarchique;            // ← Nom champ Java

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agence_id")
    private Agence agence;

    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Conge> conges = new ArrayList<>();

    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();

    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<HistoriqueCarriere> historiqueCarrieres = new ArrayList<>();

    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Demande> demandes = new ArrayList<>();

    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Evaluation> evaluations = new ArrayList<>();

    @OneToMany(mappedBy = "employe", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<Autorisation> autorisations = new ArrayList<>();

    // ==================== MÉTHODES UTILITAIRES ====================

    public String getFullName() {
        return prenom + " " + nom;
    }

    public String getNomComplet() {
        return getFullName();
    }

    public boolean hasSoldeSuffisant(int jours) {
        return this.soldeConge != null && this.soldeConge >= jours;
    }

    public void affecterDepartement(Departement departement) {
        this.departement = departement;
    }

    public void affecterPoste(Poste poste) {
        this.poste = poste;
    }

    public void affecterAgence(Agence agence) {
        this.agence = agence;
    }

    // ✅ CORRECTION : utilise chefHierarchique
    public void definirChef(Employe chef) {
        this.chefHierarchique = chef;
    }

    public Double consulterSolde() {
        return this.soldeConge;
    }

    public Map<String, Object> getInfosPersonnelles() {
        return Map.of(
                "matricule", this.matricule,
                "nom", this.nom,
                "prenom", this.prenom,
                "email", this.email,
                "telephone", this.telephone,
                "dateNaissance", this.dateNaissance,
                "adresse", this.adresse
        );
    }
}