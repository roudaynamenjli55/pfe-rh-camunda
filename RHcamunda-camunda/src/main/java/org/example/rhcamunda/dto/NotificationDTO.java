package org.example.rhcamunda.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    private String message;
    private String type; // INFO, WARNING, ERROR, SUCCESS
    private LocalDateTime timestamp;
    private String recipientMatricule;

    public NotificationDTO(String message, String type) {
        this.message = message;
        this.type = type;
        this.timestamp = LocalDateTime.now();
    }

    public NotificationDTO(String message) {
        this.message = message;
        this.type = "INFO";
        this.timestamp = LocalDateTime.now();
    }
}