package org.example.rhcamunda.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List; // Garde hadhi l import

@Entity
@Table(name = "utilisateurs")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(name = "date_creation", updatable = false)
    private LocalDateTime dateCreation;

    @Column(name = "date_modification")
    private LocalDateTime dateModification;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    // ⚠️ LES RELATIONS SUIVANTES SONT COMMENTÉES CAR LES CLASSES N'EXISTENT PAS ENCORE
    // Décommente-les seulement après avoir créé les classes Employe et LogActionDemande

    /*
    @OneToOne(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Employe employe;

    @OneToMany(mappedBy = "utilisateur", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<LogActionDemande> logActions;
    */

    // Callbacks pour les dates
    @PrePersist
    protected void onCreate() {
        this.dateCreation = LocalDateTime.now();
        this.dateModification = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.dateModification = LocalDateTime.now();
    }

    // Constructeurs
    public Utilisateur() {}

    public Utilisateur(String login, String nom, String prenom, String email,
                       String motDePasse, Role role) {
        this.login = login;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
        this.enabled = true;
    }

    // Getters & Setters
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
    public LocalDateTime getDateModification() { return dateModification; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    // ⚠️ SUPPRIME AUSSI LES GETTERS/SETTERS DE employe ET logActions ICI
    // public Employe getEmploye() { return employe; }
    // public void setEmploye(Employe employe) { this.employe = employe; }
    // public List<LogActionDemande> getLogActions() { return logActions; }
    // public void setLogActions(List<LogActionDemande> logActions) { this.logActions = logActions; }
}