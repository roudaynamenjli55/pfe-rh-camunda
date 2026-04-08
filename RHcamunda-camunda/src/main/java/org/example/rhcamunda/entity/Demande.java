package org.example.rhcamunda.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "demandes")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Demande {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employe_id")
    private Employe employe;

    @Column(nullable = false)
    private LocalDate dateCreation;

    @Column(length = 50)
    private String statut;

    @Column(length = 100)
    private String motif;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chef_hierarchique_id")
    private Employe chefHierarchique;

    @CreationTimestamp
    private LocalDateTime dateModification;

    public void verifier() {}
    public void refuser(String raison) {}
    public void approuver() {}
    public String archiver() { return "Archivé"; }
}