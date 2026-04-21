package org.example.rhcamunda.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "log_actions",
        indexes = {
                @Index(name = "idx_log_utilisateur", columnList = "utilisateur_matricule"),
                @Index(name = "idx_log_action", columnList = "action"),
                @Index(name = "idx_log_entite", columnList = "entite"),
                @Index(name = "idx_log_timestamp", columnList = "timestamp")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class LogAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    // ✅ Matricule récupéré dynamiquement depuis JWT
    @Column(nullable = false, length = 50)
    private String utilisateurMatricule;

    @Column(nullable = false, length = 100)
    private String utilisateurNom;

    // Type d'action
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ActionType action;

    // Entité concernée
    @Column(nullable = false, length = 100)
    private String entite;

    private Long entiteId;

    // Détails de l'action
    @Column(columnDefinition = "TEXT")
    private String ancienValeur;

    @Column(columnDefinition = "TEXT")
    private String nouvelleValeur;

    @Column(columnDefinition = "TEXT")
    private String details;

    // Informations techniques
    @Column(length = 45)
    private String ipAddress;

    @Column(length = 255)
    private String userAgent;

    @Column(length = 100)
    private String endpoint;

    @Column(length = 20)
    private String httpMethod;

    // Timestamp
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime timestamp;

    // Statut
    @Column(nullable = false)
    @Builder.Default
    private Boolean success = true;

    private String errorMessage;

    // Enum pour les types d'actions
    public enum ActionType {
        CREATE,
        UPDATE,
        DELETE,
        READ,
        LOGIN,
        LOGOUT,
        APPROVE,
        REJECT,
        CANCEL,
        EXPORT,
        IMPORT,
        WORKFLOW_START,
        WORKFLOW_COMPLETE,
        OTHER
    }

    // Méthodes utilitaires
    public void markSuccess() {
        this.success = true;
    }

    public void markFailure(String error) {
        this.success = false;
        this.errorMessage = error;
    }

    public boolean isCriticalAction() {
        return action == ActionType.DELETE ||
                action == ActionType.LOGIN ||
                action == ActionType.LOGOUT;
    }
}