package org.example.rhcamunda.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "postes")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Poste {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String titre;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String niveau;

    @OneToMany(mappedBy = "poste", fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @Builder.Default
    private List<Employe> employes = new ArrayList<>();

    // --- Méthodes du Diagramme ---
    public void ajouter(Employe employe) {
        this.employes.add(employe);
        employe.setPoste(this);
    }

    public void modifier() {
        // Logique de mise à jour
    }

    public List<Employe> obtenirEmployes() {
        return employes;
    }
}