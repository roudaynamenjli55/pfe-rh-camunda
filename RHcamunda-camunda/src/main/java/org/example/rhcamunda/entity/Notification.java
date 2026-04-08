package org.example.rhcamunda.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "notifications")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Notification {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id")
    private Employe employe;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    private LocalDateTime dateEnvoi;

    @Builder.Default
    private Boolean lu = false;

    @Column(length = 50)
    private String type;

    private String limiteAction;

    @Builder.Default
    private Integer priorite = 0;

    // --- Méthodes du Diagramme ---
    public void envoyer() {
        this.dateEnvoi = LocalDateTime.now();
    }

    public void marquerCommeLue() {
        this.lu = true;
    }

    public void supprimer() {
        // Logique de suppression (physique ou soft delete)
    }

    public Map<String, Object> obtenirDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("id", this.id);
        details.put("message", this.message);
        details.put("dateEnvoi", this.dateEnvoi);
        details.put("lu", this.lu);
        details.put("priorite", this.priorite);
        return details;
    }
}