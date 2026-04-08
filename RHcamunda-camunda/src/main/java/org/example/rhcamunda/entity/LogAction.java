package org.example.rhcamunda.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "log_actions")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LogAction {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur utilisateur;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(nullable = false)
    private LocalDateTime dateAction;

    @Column(length = 255)
    private String details;

    @Column(length = 50)
    private String adresseIP;

    @Column(length = 255)
    private String ancienneValeur;

    @Column(length = 255)
    private String nouvelleValeur;

    public void enregistrer() {
        // Logique d'enregistrement
    }

    public List<LogAction> obtenirHistoriquePeriode(LocalDateTime debut, LocalDateTime fin) {
        return List.of();
    }

    public void exporterRapport(LocalDateTime debut, LocalDateTime fin) {
        // Export logic
    }

    public List<LogAction> obtenirHistoriqueUtilisateur(Utilisateur utilisateur) {
        return List.of();
    }
}