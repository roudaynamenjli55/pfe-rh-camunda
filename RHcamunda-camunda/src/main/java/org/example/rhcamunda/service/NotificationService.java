package org.example.rhcamunda.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rhcamunda.dto.NotificationDTO;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotificationToUser(String matricule, String message) {
        log.info("Envoi notification à {}: {}", matricule, message);

        NotificationDTO notification = NotificationDTO.builder()
                .message(message)
                .type("INFO")
                .recipientMatricule(matricule)
                .build();

        messagingTemplate.convertAndSendToUser(
                matricule,
                "/topic/notifications",
                notification
        );
    }

    public void sendSuccessNotification(String matricule, String message) {
        NotificationDTO notification = new NotificationDTO(message, "SUCCESS");
        messagingTemplate.convertAndSendToUser(matricule, "/topic/notifications", notification);
    }

    public void sendErrorNotification(String matricule, String message) {
        NotificationDTO notification = new NotificationDTO(message, "ERROR");
        messagingTemplate.convertAndSendToUser(matricule, "/topic/notifications", notification);
    }

    public void notifyCongeApprouve(String matriculeEmploye, Long congeId) {
        String message = String.format("Votre demande de congé #%d a été approuvée", congeId);
        sendSuccessNotification(matriculeEmploye, message);
    }

    public void notifyCongeRefuse(String matriculeEmploye, Long congeId, String motif) {
        String message = String.format("Votre demande de congé #%d a été refusée: %s", congeId, motif);
        sendErrorNotification(matriculeEmploye, message);
    }

    public void notifyNouvelleDemande(String matriculeManager, String nomEmploye) {
        String message = String.format("Nouvelle demande de congé de %s en attente de validation", nomEmploye);
        sendNotificationToUser(matriculeManager, message);
    }
}