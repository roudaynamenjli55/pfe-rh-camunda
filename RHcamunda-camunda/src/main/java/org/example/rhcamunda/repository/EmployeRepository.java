package org.example.rhcamunda.repository;

import org.example.rhcamunda.entity.Employe;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, Long> {

    // =================================================================
    // 🔹 RECHERCHE SIMPLE
    // =================================================================

    Optional<Employe> findByMatricule(String matricule);
    Optional<Employe> findByEmail(String email);

    // =================================================================
    // 🔹 FILTRES MÉTIER
    // =================================================================

    List<Employe> findByDepartementId(Long departementId);

    // 🔹 Employés actifs seulement (cohérent avec le champ 'actif')
    List<Employe> findByActifTrue();

    // 🔹 Pour la suppression logique (archive)
    List<Employe> findByActifFalse();

    // =================================================================
    // 🔹 RECHERCHE AVANCÉE
    // =================================================================

    @Query("SELECT e FROM Employe e WHERE e.nom LIKE %:keyword% OR e.prenom LIKE %:keyword%")
    List<Employe> searchByName(@Param("keyword") String keyword);

    @Query("SELECT e FROM Employe e WHERE e.departement.id = :departementId AND e.actif = true")
    List<Employe> findByDepartementAndActif(@Param("departementId") Long departementId);

    // =================================================================
    // 🔹 PAGINATION
    // =================================================================

    Page<Employe> findAll(Pageable pageable);

    Page<Employe> findByActifTrue(Pageable pageable);

    // =================================================================
    // 🔹 VÉRIFICATIONS UNIQUETÉ (pour le matricule auto-généré)
    // =================================================================

    // ✅ Vérifie si un matricule existe déjà
    boolean existsByMatricule(String matricule);

    // ✅ Compte les employés embauchés dans une période (pour générer le numéro séquentiel)
    long countByDateEmbaucheBetween(@Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate);

    // ✅ Compte tous les employés actifs (pour stats)
    long countByActifTrue();
}