package org.example.rhcamunda.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "agences")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Agence {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(columnDefinition = "TEXT")
    private String adresse;

    @Column(length = 20)
    private String telephone;

    private LocalDateTime dateCreation;

    @OneToMany(mappedBy = "agence", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<Employe> employes = new ArrayList<>();

    // --- Méthodes du Diagramme ---

    public void ajouter(Employe employe) {
        this.employes.add(employe);
        employe.setAgence(this);
    }

    public void modifier() {
        // Logique de mise à jour
    }

    public List<Employe> obtenirEmployes() {
        return employes;
    }

    // ✅ CORRECTION : Compter les employés ACTIFS seulement
    public Integer obtenirEffectif() {
        if (employes == null) {
            return 0;
        }
        return (int) employes.stream()
                .filter(e -> e.getActif() != null && e.getActif()) // ← Actif = true
                .count();
    }

    // 🔹 Bonus : Méthode pour compter les inactifs (si besoin)
    public Integer obtenirEffectifInactif() {
        if (employes == null) {
            return 0;
        }
        return (int) employes.stream()
                .filter(e -> e.getActif() == null || !e.getActif()) // ← Actif = false
                .count();
    }
}