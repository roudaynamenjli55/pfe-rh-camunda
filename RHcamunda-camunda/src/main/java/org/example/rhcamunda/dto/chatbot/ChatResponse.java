package org.example.rhcamunda.dto.chatbot;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Réponse du chatbot envoyée au frontend Angular.
 */
@Data
@Builder
public class ChatResponse {

    /** Réponse textuelle du chatbot */
    private String message;

    /** ID de session retourné pour que le frontend le conserve */
    private String sessionId;

    /** Intent détecté (ex: SOLDE_CONGE, DEMANDE_CONGE) — optionnel */
    private String intent;

    /**
     * Données d'action pour le frontend :
     * ex: { "action": "REDIRECT", "route": "/conges/nouvelle-demande", "label": "..." }
     */
    private Object actionData;

    private LocalDateTime timestamp;

    private boolean success;

    /** Code d'erreur pour traçabilité (contexte bancaire) */
    private String errorCode;
}