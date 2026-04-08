package org.example.rhcamunda.repository;

import org.example.rhcamunda.entity.Employe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, Long> {

    // Chercher par matricule (ID unique employé)
    Optional<Employe> findByMatricule(String matricule);

    // Chercher par email
    Optional<Employe> findByEmail(String email);

    // Chercher les employés d'un département
    List<Employe> findByDepartementId(Long departementId);

    // Chercher les employés actifs seulement
    List<Employe> findByArchiveFalse();

    // Recherche avancée (Nom contient...)
    @Query("SELECT e FROM Employe e WHERE e.nom LIKE %:keyword% OR e.prenom LIKE %:keyword%")
    List<Employe> searchByName(@Param("keyword") String keyword);

    // Pagination (pour les listes longues)
    Page<Employe> findAll(Pageable pageable);
}