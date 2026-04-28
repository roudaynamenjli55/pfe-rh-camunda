package org.example.rhcamunda.service.chatbot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Gère l'historique des conversations dans Redis.
 * Clé : chatbot:history:{employeId}:{sessionId}
 * TTL : 30 minutes d'inactivité
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationMemoryService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String KEY_PREFIX = "chatbot:history:";
    /** Expiration après 30 minutes d'inactivité */
    private static final Duration TTL = Duration.ofMinutes(30);
    /** Limite de messages conservés en mémoire pour éviter l'explosion de tokens */
    private static final int MAX_MESSAGES = 10;

    /**
     * Récupère l'historique de conversation depuis Redis.
     * Retourne une liste vide si aucune session n'existe.
     */
    public List<Message> getHistory(String employeId, String sessionId) {
        String key = buildKey(employeId, sessionId);
        String raw = redisTemplate.opsForValue().get(key);

        if (raw == null) return new ArrayList<>();

        try {
            List<Map<String, String>> stored = objectMapper.readValue(
                    raw, new TypeReference<>() {}
            );
            List<Message> messages = new ArrayList<>();
            for (Map<String, String> entry : stored) {
                if ("USER".equals(entry.get("role"))) {
                    messages.add(new UserMessage(entry.get("content")));
                } else {
                    messages.add(new AssistantMessage(entry.get("content")));
                }
            }
            return messages;
        } catch (Exception e) {
            log.error("Erreur lecture historique Redis pour {}", key, e);
            return new ArrayList<>();
        }
    }

    /**
     * Sauvegarde l'historique de conversation dans Redis.
     * Conserve uniquement les MAX_MESSAGES derniers messages.
     */
    public void saveHistory(String employeId, String sessionId, List<Message> messages) {
        String key = buildKey(employeId, sessionId);

        // Garder seulement les N derniers messages (sliding window)
        List<Message> trimmed = messages.size() > MAX_MESSAGES
                ? messages.subList(messages.size() - MAX_MESSAGES, messages.size())
                : messages;

        try {
            List<Map<String, String>> toStore = new ArrayList<>();
            for (Message msg : trimmed) {
                String role = (msg instanceof UserMessage) ? "USER" : "ASSISTANT";
                toStore.add(Map.of("role", role, "content", msg.getText()));
            }
            String json = objectMapper.writeValueAsString(toStore);
            redisTemplate.opsForValue().set(key, json, TTL);
        } catch (Exception e) {
            log.error("Erreur sauvegarde historique Redis pour {}", key, e);
        }
    }

    /**
     * Supprime une session de conversation (appelé par le controller DELETE /session/{id}).
     */
    public void clearSession(String employeId, String sessionId) {
        redisTemplate.delete(buildKey(employeId, sessionId));
        log.debug("Session chatbot effacée: {}:{}", employeId, sessionId);
    }

    private String buildKey(String employeId, String sessionId) {
        return KEY_PREFIX + employeId + ":" + sessionId;
    }
}