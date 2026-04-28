package org.example.rhcamunda.service.chatbot;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rhcamunda.dto.chatbot.ChatRequest;
import org.example.rhcamunda.dto.chatbot.ChatResponse;
import org.example.rhcamunda.entity.Employe;
import org.example.rhcamunda.repository.EmployeRepository;
import org.example.rhcamunda.util.UserContext;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service principal du chatbot RH IA.
 * Orchestre : contexte employé → prompt système → historique Redis → appel OpenAI.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatbotService {

    private final ChatClient chatClient;
    private final SystemPromptBuilder systemPromptBuilder;
    private final RhToolsService rhToolsService;
    private final ConversationMemoryService memoryService;
    private final UserContext userContext;
    private final EmployeRepository employeRepository;

    public ChatResponse processMessage(ChatRequest request) {

        // 1. Récupérer le matricule via UserContext (même pattern que CongeService)
        String matricule = userContext.getCurrentUserMatricule();

        // 2. Charger l'employé depuis la BDD
        Employe employe = employeRepository.findByMatricule(matricule)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé : " + matricule));

        // 3. Session ID (persistance historique)
        String sessionId = (request.getSessionId() != null && !request.getSessionId().isBlank())
                ? request.getSessionId()
                : UUID.randomUUID().toString();

        log.debug("Chatbot — matricule: {}, session: {}", matricule, sessionId);

        try {
            // 4. Prompt système personnalisé avec les données de l'employé
            String systemPrompt = systemPromptBuilder.build(employe);

            // 5. Contexte RH dynamique (soldes, autorisations, procédures...)
            String rhContext = rhToolsService.buildRhContext(request.getMessage(), employe);

            String fullSystemPrompt = rhContext.isBlank()
                    ? systemPrompt
                    : systemPrompt + "\n\nDONNÉES RH EN TEMPS RÉEL :\n" + rhContext;

            // 6. Historique de conversation depuis Redis
            List<Message> history = memoryService.getHistory(
                    employe.getId().toString(), sessionId
            );

            // 7. Construction du prompt complet (system + historique + message utilisateur)
            List<Message> messages = new ArrayList<>();
            messages.add(new SystemMessage(fullSystemPrompt));
            messages.addAll(history);
            messages.add(new UserMessage(request.getMessage()));

            // 8. Appel OpenAI via Spring AI
            Prompt prompt = new Prompt(messages);
            String aiMessage = chatClient.prompt(prompt)
                    .call()
                    .chatResponse()
                    .getResult()
                    .getOutput()
                    .getText();

            // 9. Sauvegarder l'échange dans Redis pour maintenir le contexte
            history.add(new UserMessage(request.getMessage()));
            history.add(new AssistantMessage(aiMessage));
            memoryService.saveHistory(employe.getId().toString(), sessionId, history);

            // 10. Détecter une action frontend (redirection vers formulaire...)
            Object actionData = detectActionData(aiMessage, request.getMessage());

            return ChatResponse.builder()
                    .message(aiMessage)
                    .sessionId(sessionId)
                    .actionData(actionData)
                    .timestamp(LocalDateTime.now())
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Erreur OpenAI — matricule: {}, session: {}", matricule, sessionId, e);
            return ChatResponse.builder()
                    .message("Je rencontre une difficulté technique. " +
                            "Veuillez réessayer ou contacter le service RH.")
                    .sessionId(sessionId)
                    .success(false)
                    .errorCode("AI_SERVICE_ERROR")
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    /**
     * Détecte si la conversation implique une action frontend (redirection vers un formulaire).
     * Le frontend Angular peut utiliser actionData pour proposer un bouton de navigation.
     */
    private Object detectActionData(String aiMessage, String userMessage) {
        String combined = (aiMessage + " " + userMessage).toLowerCase();

        if (containsAny(combined, "formulaire de congé", "demande de congé",
                "soumettre un congé", "nouvelle demande de congé")) {
            return Map.of(
                    "action", "REDIRECT",
                    "route", "/conges/nouvelle-demande",
                    "label", "Soumettre une demande de congé"
            );
        }
        if (containsAny(combined, "formulaire d'autorisation",
                "demande d'autorisation", "soumettre autorisation")) {
            return Map.of(
                    "action", "REDIRECT",
                    "route", "/autorisations/nouvelle-demande",
                    "label", "Demander une autorisation"
            );
        }
        if (containsAny(combined, "attestation", "certificat de travail",
                "attestation de salaire")) {
            return Map.of(
                    "action", "REDIRECT",
                    "route", "/demandes/nouvelle-demande",
                    "label", "Demander une attestation"
            );
        }
        return null;
    }

    private boolean containsAny(String text, String... keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) return true;
        }
        return false;
    }
}