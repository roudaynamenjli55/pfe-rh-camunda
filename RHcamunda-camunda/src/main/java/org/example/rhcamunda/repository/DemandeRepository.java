package org.example.rhcamunda.repository;

import org.example.rhcamunda.entity.Demande;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface DemandeRepository extends JpaRepository<Demande, Long> {

    List<Demande> findByStatut(String statut);

    List<Demande> findByDateCreationBetween(LocalDateTime debut, LocalDateTime fin);

    long countByTypeAndDateCreationBetween(String type, LocalDateTime debut, LocalDateTime fin);

    // =================================================================
    // 🔹 RECHERCHES PAR MATRICULE (DYNAMIQUE)
    // =================================================================

    /**
     * Trouver toutes les demandes d'un employé par son matricule
     * Triées par date de création (plus récente en premier)
     */
    @Query("SELECT d FROM Demande d JOIN FETCH d.employe WHERE d.employe.matricule = :matricule ORDER BY d.dateCreation DESC")
    List<Demande> findByEmployeMatricule(@Param("matricule") String matricule);

    /**
     * Trouver les demandes par matricule et statut
     */
    @Query("SELECT d FROM Demande d JOIN FETCH d.employe WHERE d.employe.matricule = :matricule AND d.statut = :statut ORDER BY d.dateCreation DESC")
    List<Demande> findByEmployeMatriculeAndStatut(
            @Param("matricule") String matricule,
            @Param("statut") String statut
    );

    /**
     * Trouver les demandes par matricule et période
     */
    @Query("SELECT d FROM Demande d JOIN FETCH d.employe WHERE d.employe.matricule = :matricule " +
            "AND d.dateCreation BETWEEN :dateDebut AND :dateFin " +
            "ORDER BY d.dateCreation DESC")
    List<Demande> findByEmployeMatriculeAndPeriode(
            @Param("matricule") String matricule,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin
    );

    // =================================================================
    // 🔹 COMPTAGE DYNAMIQUE
    // =================================================================

    /**
     * Compter le nombre de demandes par employé (matricule)
     */
    @Query("SELECT COUNT(d) FROM Demande d WHERE d.employe.matricule = :matricule")
    long countByEmployeMatricule(@Param("matricule") String matricule);

    /**
     * Compter les demandes par employé et statut
     */
    @Query("SELECT COUNT(d) FROM Demande d WHERE d.employe.matricule = :matricule AND d.statut = :statut")
    long countByEmployeMatriculeAndStatut(
            @Param("matricule") String matricule,
            @Param("statut") String statut
    );

    /**
     * Compter les demandes par période et statut
     */
    @Query("SELECT COUNT(d) FROM Demande d WHERE d.employe.matricule = :matricule " +
            "AND d.dateCreation BETWEEN :dateDebut AND :dateFin " +
            "AND d.statut = :statut")
    long countByEmployeMatriculeAndPeriodeAndStatut(
            @Param("matricule") String matricule,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin,
            @Param("statut") String statut
    );

    // =================================================================
    // 🔹 RECHERCHES AVANCÉES (MULTI-CRITÈRES)
    // =================================================================

    /**
     * Recherche dynamique avec plusieurs critères optionnels
     */
    @Query("SELECT d FROM Demande d JOIN FETCH d.employe WHERE " +
            "(:matricule IS NULL OR d.employe.matricule = :matricule) " +
            "AND (:statut IS NULL OR d.statut = :statut) " +
            "AND (:type IS NULL OR d.motif LIKE CONCAT('%', :type, '%')) " +
            "AND (:dateDebut IS NULL OR d.dateCreation >= :dateDebut) " +
            "AND (:dateFin IS NULL OR d.dateCreation <= :dateFin) " +
            "ORDER BY d.dateCreation DESC")
    List<Demande> findByCritereDynamique(
            @Param("matricule") String matricule,
            @Param("statut") String statut,
            @Param("type") String type,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("dateFin") LocalDate dateFin
    );

    /**
     * Trouver les demandes en attente de validation par le chef hiérarchique
     */
    @Query("SELECT d FROM Demande d JOIN FETCH d.employe WHERE d.chefHierarchique.matricule = :matriculeChef " +
            "AND d.statut = 'EN_ATTENTE' ORDER BY d.dateCreation ASC")
    List<Demande> findDemandesEnAttenteParChef(
            @Param("matriculeChef") String matriculeChef
    );

    /**
     * Trouver les demandes validées/rejetées par un chef
     */
    @Query("SELECT d FROM Demande d JOIN FETCH d.employe WHERE d.chefHierarchique.matricule = :matriculeChef " +
            "AND d.statut IN ('VALIDEE', 'REJETEE') " +
            "ORDER BY d.dateValidation DESC")
    List<Demande> findDemandesTraiteesParChef(
            @Param("matriculeChef") String matriculeChef
    );

    // =================================================================
    // 🔹 STATISTIQUES & RAPPORTS
    // =================================================================

    /**
     * Statistiques par mois pour un employé
     */
    @Query("SELECT FUNCTION('DATE_TRUNC', 'month', d.dateCreation) as mois, " +
            "COUNT(d) as nombre, d.statut " +
            "FROM Demande d " +
            "WHERE d.employe.matricule = :matricule " +
            "GROUP BY FUNCTION('DATE_TRUNC', 'month', d.dateCreation), d.statut " +
            "ORDER BY mois DESC")
    List<Object[]> getStatistiquesMensuellesParEmploye(@Param("matricule") String matricule);

    /**
     * Nombre total de demandes par statut (pour dashboard RH)
     */
    @Query("SELECT d.statut, COUNT(d) FROM Demande d GROUP BY d.statut")
    List<Object[]> getStatistiquesGlobalesParStatut();

    /**
     * Demandes par période pour un rapport
     */
    @Query("SELECT d FROM Demande d JOIN FETCH d.employe WHERE d.dateCreation BETWEEN :debut AND :fin " +
            "ORDER BY d.dateCreation DESC")
    List<Demande> findByPeriode(
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin
    );

    // =================================================================
    // 📄 PAGINATION (Angular Material)
    // =================================================================

    @Query(value = "SELECT d FROM Demande d JOIN FETCH d.employe WHERE d.employe.matricule = :matricule ORDER BY d.dateCreation DESC",
           countQuery = "SELECT COUNT(d) FROM Demande d WHERE d.employe.matricule = :matricule")
    Page<Demande> findByEmployeMatriculePagine(@Param("matricule") String matricule, Pageable pageable);

    @Query(value = "SELECT d FROM Demande d JOIN FETCH d.employe WHERE d.statut = :statut ORDER BY d.dateCreation DESC",
           countQuery = "SELECT COUNT(d) FROM Demande d WHERE d.statut = :statut")
    Page<Demande> findByStatutPagine(@Param("statut") String statut, Pageable pageable);

    @Query(value = "SELECT d FROM Demande d JOIN FETCH d.employe ORDER BY d.dateCreation DESC",
           countQuery = "SELECT COUNT(d) FROM Demande d")
    Page<Demande> findAllPagine(Pageable pageable);
}