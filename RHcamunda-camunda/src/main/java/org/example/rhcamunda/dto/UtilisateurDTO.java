package org.example.rhcamunda.dto;

import java.time.LocalDateTime;

public class UtilisateurDTO {
    private Long id;
    private String login;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse; // Uniquement pour la création/mise à jour
    private Boolean enabled;
    private LocalDateTime dateCreation;

    // Informations du rôle
    private Long roleId;
    private String roleCode;
    private String roleLibelle;

    // Constructeur vide (nécessaire pour Jackson)
    public UtilisateurDTO() {}

    // Getters & Setters (Génère-les avec Alt+Insert)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public Long getRoleId() { return roleId; }
    public void setRoleId(Long roleId) { this.roleId = roleId; }

    public String getRoleCode() { return roleCode; }
    public void setRoleCode(String roleCode) { this.roleCode = roleCode; }

    public String getRoleLibelle() { return roleLibelle; }
    public void setRoleLibelle(String roleLibelle) { this.roleLibelle = roleLibelle; }
}