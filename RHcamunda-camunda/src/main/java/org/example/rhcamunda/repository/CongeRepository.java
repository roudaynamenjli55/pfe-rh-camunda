package org.example.rhcamunda.repository;

import org.example.rhcamunda.entity.Conge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CongeRepository extends JpaRepository<Conge, Long> {

    // ========================================================================
    // 🔹 RECHERCHES PAR EMPLOYÉ (ID)
    // ========================================================================

    /**
     * Trouver toutes les demandes d'un employé
     */
    List<Conge> findByEmployeId(Long employeId);

    /**
     * Trouver les demandes d'un employé par statut
     */
    List<Conge> findByEmployeIdAndStatut(Long employeId, String statut);

    /**
     * Trouver les demandes validées d'un employé
     */
    List<Conge> findByStatutAndEmployeId(String statut, Long employeId);

    /**
     * Trouver les demandes d'un employé dans une période
     */
    @Query("SELECT c FROM Conge c WHERE c.employe.id = :employeId AND c.dateDebut BETWEEN :debut AND :fin")
    List<Conge> findByEmployeIdAndDateBetween(@Param("employeId") Long employeId,
                                              @Param("debut") LocalDate debut,
                                              @Param("fin") LocalDate fin);

    // ========================================================================
    // 🔹 RECHERCHES PAR EMPLOYÉ (MATRICULE) - Pour EvaluationService ✅
    // ========================================================================

    /**
     * ⭐ CRITIQUE: Compter congés validés pour auto-évaluation
     * Utilisé dans EvaluationService.calculerNombreCongesCampagne()
     */
    @Query("SELECT COUNT(c) FROM Conge c " +
            "WHERE c.employe.matricule = :matricule " +
            "AND c.dateDebut BETWEEN :debut AND :fin " +
            "AND c.statut = :statut")
    Integer countByEmployeMatriculeAndDateDebutBetweenAndStatut(
            @Param("matricule") String matricule,
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin,
            @Param("statut") String statut);

    /**
     * Liste des congés par matricule (pour détails évaluation)
     */
    @Query("SELECT c FROM Conge c " +
            "WHERE c.employe.matricule = :matricule " +
            "AND c.dateDebut BETWEEN :debut AND :fin " +
            "AND c.statut = :statut")
    List<Conge> findByEmployeMatriculeAndPeriodAndStatut(
            @Param("matricule") String matricule,
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin,
            @Param("statut") String statut);

    // ========================================================================
    // 🔹 WORKFLOW CAMUNDA
    // ========================================================================

    /**
     * TROUVER UNE DEMANDE PAR L'ID DU PROCESSUS CAMUNDA (TRÈS IMPORTANT!)
     */
    Optional<Conge> findByProcessInstanceId(String processInstanceId);

    // ========================================================================
    // 🔹 GESTION DES STATUTS
    // ========================================================================

    /**
     * Trouver les demandes en attente (tous employés)
     */
    List<Conge> findByStatut(String statut);

    /**
     * Trouver les demandes en attente de validation par un manager
     */
    @Query("SELECT c FROM Conge c WHERE c.employe.chefHierarchique.matricule = :matriculeChef AND c.statut = :statut")
    List<Conge> findByChefMatriculeAndStatut(@Param("matriculeChef") String matriculeChef,
                                             @Param("statut") String statut);

    // ========================================================================
    // 🔹 STATISTIQUES & DASHBOARD RH
    // ========================================================================

    /**
     * Compter congés par statut dans une période (dashboard)
     */
    @Query("SELECT COUNT(c) FROM Conge c WHERE c.statut = :statut AND c.dateDebut BETWEEN :debut AND :fin")
    long countByStatutAndDateBetween(@Param("statut") String statut,
                                     @Param("debut") LocalDate debut,
                                     @Param("fin") LocalDate fin);

    /**
     * Compter congés par type dans une période
     */
    @Query("SELECT COUNT(c) FROM Conge c WHERE c.typeConge = :type AND c.dateDebut BETWEEN :debut AND :fin")
    long countByTypeAndDateBetween(@Param("type") String type,
                                   @Param("debut") LocalDate debut,
                                   @Param("fin") LocalDate fin);

    /**
     * Compter congés par département dans une période
     */
    @Query("SELECT COUNT(c) FROM Conge c " +
            "WHERE c.employe.departement.id = :departementId " +
            "AND c.dateDebut BETWEEN :debut AND :fin")
    long countByDepartementAndPeriod(@Param("departementId") Long departementId,
                                     @Param("debut") LocalDate debut,
                                     @Param("fin") LocalDate fin);

    /**
     * Taux d'absentéisme: jours de congés consommés par période
     */
    @Query("SELECT SUM(c.nbjours) FROM Conge c " +
            "WHERE c.employe.departement.id = :departementId " +
            "AND c.statut = 'APPROUVE'" +
            "AND c.dateDebut BETWEEN :debut AND :fin")
    Optional<Long> sumJoursByDepartementAndPeriod(@Param("departementId") Long departementId,
                                                  @Param("debut") LocalDate debut,
                                                  @Param("fin") LocalDate fin);

    // ========================================================================
    // 🔹 EXPORT & RAPPORTS
    // ========================================================================

    /**
     * Congés validés pour export mensuel
     */
    @Query("SELECT c FROM Conge c " +
            "WHERE c.statut = 'APPROUVE' " +
            "AND FUNCTION('DATE_TRUNC', 'month', c.dateDebut) = :mois " +
            "ORDER BY c.dateDebut DESC")
    List<Conge> findValidatedByMonth(@Param("mois") LocalDate mois);

    /**
     * Congés par agence pour reporting
     */
    List<Conge> findByEmploye_AgenceIdOrderByDateDebutDesc(Long agenceId);

    // ========================================================================
    // 🔹 SOLDES & QUOTAS
    // ========================================================================

    /**
     * Vérifier si un employé a déjà une demande de congé à une date (éviter doublons)
     */
    boolean existsByEmployeIdAndDateDebutAndAndTypeCongeAndAndStatutIn(
            @Param("employeId") Long employeId,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("typeConge") String typeConge ,
            @Param("statuts") List<String> statuts);

    /**
     * Calculer le total des jours de congés consommés par un employé dans l'année
     */
    @Query("SELECT COALESCE(SUM(c.nbjours), 0) FROM Conge c " +
            "WHERE c.employe.id = :employeId " +
            "AND c.statut = 'APPROUVE' " +
            "AND FUNCTION('YEAR', c.dateDebut) = :annee")
    Integer sumJoursConsommesByEmployeAndYear(@Param("employeId") Long employeId,
                                              @Param("annee") Integer annee);
}