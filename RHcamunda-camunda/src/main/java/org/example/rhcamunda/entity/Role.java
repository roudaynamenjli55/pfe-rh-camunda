package org.example.rhcamunda.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // ex: ADMIN, USER, MANAGER

    @Column(nullable = false)
    private String libelle; // ex: Administrateur, Utilisateur

    private String description;

    @Column(name = "est_actif")
    private Boolean estActif = true;

    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
    private List<Utilisateur> utilisateurs;

    // Constructeurs
    public Role() {}

    public Role(String code, String libelle) {
        this.code = code;
        this.libelle = libelle;
        this.estActif = true;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLibelle() { return libelle; }
    public void setLibelle(String libelle) { this.libelle = libelle; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getEstActif() { return estActif; }
    public void setEstActif(Boolean estActif) { this.estActif = estActif; }

    public List<Utilisateur> getUtilisateurs() { return utilisateurs; }
    public void setUtilisateurs(List<Utilisateur> utilisateurs) { this.utilisateurs = utilisateurs; }
}