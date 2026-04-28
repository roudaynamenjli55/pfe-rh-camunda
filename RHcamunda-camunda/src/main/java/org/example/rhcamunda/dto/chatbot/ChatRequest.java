package org.example.rhcamunda.dto.chatbot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Requête envoyée par le frontend au chatbot.
 */
@Data
public class ChatRequest {

    @NotBlank(message = "Le message ne peut pas être vide")
    @Size(max = 1000, message = "Message trop long (max 1000 caractères)")
    private String message;

    /**
     * ID de session pour maintenir l'historique de conversation.
     * Si null ou blank, une nouvelle session est créée.
     */
    private String sessionId;
}