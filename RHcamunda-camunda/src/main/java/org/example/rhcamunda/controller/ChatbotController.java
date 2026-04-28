package org.example.rhcamunda.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rhcamunda.dto.chatbot.ChatRequest;
import org.example.rhcamunda.dto.chatbot.ChatResponse;
import org.example.rhcamunda.service.chatbot.AiChatbotService;
import org.example.rhcamunda.service.chatbot.ConversationMemoryService;
import org.example.rhcamunda.util.UserContext;
import org.example.rhcamunda.repository.EmployeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
@Slf4j
public class ChatbotController {

    private final AiChatbotService chatbotService;
    private final ConversationMemoryService memoryService;
    private final UserContext userContext;
    private final EmployeRepository employeRepository;

    /**
     * Envoyer un message au chatbot RH
     * Accessible par tous les utilisateurs authentifiés
     */
    @PostMapping("/message")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ChatResponse> sendMessage(
            @Valid @RequestBody ChatRequest request) {
        log.debug("Message chatbot reçu, session: {}", request.getSessionId());
        ChatResponse response = chatbotService.processMessage(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Effacer l'historique d'une session de conversation
     */
    @DeleteMapping("/session/{sessionId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> clearSession(@PathVariable String sessionId) {
        try {
            String matricule = userContext.getCurrentUserMatricule();
            employeRepository.findByMatricule(matricule).ifPresent(employe ->
                memoryService.clearSession(employe.getId().toString(), sessionId)
            );
        } catch (Exception e) {
            log.warn("Impossible d'effacer la session {}: {}", sessionId, e.getMessage());
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * Health check du chatbot (endpoint public)
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Chatbot IA opérationnel");
    }
}