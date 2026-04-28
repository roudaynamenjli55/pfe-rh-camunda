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
    List<Conge> findByEmployeId(Long employeId);

    List<Conge> findByEmployeIdOrderByDateDebutDesc(Long employeId); // ✅ ADD THIS

    List<Conge> findByEmployeIdAndStatut(Long employeId, String statut);

    @Query("SELECT c FROM Conge c JOIN FETCH c.employe WHERE c.employe.id = :employeId AND c.dateDebut BETWEEN :debut AND :fin")
    List<Conge> findByEmployeIdAndDateBetween(@Param("employeId") Long employeId,
                                              @Param("debut") LocalDate debut,
                                              @Param("fin") LocalDate fin);

    // ========================================================================
    // 🔹 RECHERCHES PAR EMPLOYÉ (MATRICULE) - Pour EvaluationService ✅
    // ========================================================================

    @Query("SELECT COUNT(c) FROM Conge c " +
            "WHERE c.employe.matricule = :matricule " +
            "AND c.dateDebut BETWEEN :debut AND :fin " +
            "AND c.statut = :statut")
    Integer countByEmployeMatriculeAndDateDebutBetweenAndStatut(
            @Param("matricule") String matricule,
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin,
            @Param("statut") String statut);

    @Query("SELECT c FROM Conge c JOIN FETCH c.employe " +
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

    Optional<Conge> findByProcessInstanceId(String processInstanceId);

    // ========================================================================
    // 🔹 GESTION DES STATUTS
    // ========================================================================

    List<Conge> findByStatut(String statut);

    @Query("SELECT c FROM Conge c JOIN FETCH c.employe WHERE c.employe.chefHierarchique.matricule = :matriculeChef AND c.statut = :statut")
    List<Conge> findByChefMatriculeAndStatut(@Param("matriculeChef") String matriculeChef,
                                             @Param("statut") String statut);

    // ========================================================================
    // 🔹 STATISTIQUES & DASHBOARD RH - ✅ NOUVELLES REQUÊTES DYNAMIQUES
    // ========================================================================

    @Query("SELECT COUNT(c) FROM Conge c WHERE c.statut = :statut AND c.dateDebut BETWEEN :debut AND :fin")
    long countByStatutAndDateBetween(@Param("statut") String statut,
                                     @Param("debut") LocalDate debut,
                                     @Param("fin") LocalDate fin);

    @Query("SELECT COUNT(c) FROM Conge c WHERE c.typeConge = :typeConge AND c.dateDebut BETWEEN :debut AND :fin")
    long countByTypeAndDateBetween(@Param("typeConge") String typeConge,
                                   @Param("debut") LocalDate debut,
                                   @Param("fin") LocalDate fin);

    @Query("SELECT COUNT(c) FROM Conge c " +
            "WHERE c.employe.departement.id = :departementId " +
            "AND c.dateDebut BETWEEN :debut AND :fin")
    long countByDepartementAndPeriod(@Param("departementId") Long departementId,
                                     @Param("debut") LocalDate debut,
                                     @Param("fin") LocalDate fin);

    @Query("SELECT SUM(c.nbjours) FROM Conge c " +
            "WHERE c.employe.departement.id = :departementId " +
            "AND c.statut = 'APPROUVE' " +
            "AND c.dateDebut BETWEEN :debut AND :fin")
    Optional<Long> sumJoursByDepartementAndPeriod(@Param("departementId") Long departementId,
                                                  @Param("debut") LocalDate debut,
                                                  @Param("fin") LocalDate fin);

    // ✅ NOUVEAU : Comptage simple par statut (sans dates) - pour dashboard
    @Query("SELECT COUNT(c) FROM Conge c WHERE c.statut = :statut")
    long countByStatut(@Param("statut") String statut);

    // ✅ NOUVEAU : Total jours approuvés cette année - pour stats congés
    @Query("SELECT COALESCE(SUM(c.nbjours), 0) FROM Conge c " +
            "WHERE c.statut = 'APPROUVE' AND c.dateDebut >= :debutAnnee")
    long sumApprovedDaysThisYear(@Param("debutAnnee") LocalDate debutAnnee);

    // ✅ NOUVEAU : Total jours d'absence ce mois - pour taux d'absentéisme
    @Query("SELECT COALESCE(SUM(c.nbjours), 0) FROM Conge c " +
            "WHERE c.statut = 'APPROUVE' AND c.dateDebut >= :debutMois")
    long sumAbsenceDaysThisMonth(@Param("debutMois") LocalDate debutMois);

    // ========================================================================
    // 🔹 EXPORT & RAPPORTS
    // ========================================================================

    @Query("SELECT c FROM Conge c JOIN FETCH c.employe " +
            "WHERE c.statut = 'APPROUVE' " +
            "AND FUNCTION('DATE_TRUNC', 'month', c.dateDebut) = :mois " +
            "ORDER BY c.dateDebut DESC")
    List<Conge> findValidatedByMonth(@Param("mois") LocalDate mois);

    List<Conge> findByEmploye_AgenceIdOrderByDateDebutDesc(Long agenceId);

    // ========================================================================
    // 🔹 SOLDES & QUOTAS
    // ========================================================================

    // ✅ CORRECTION : Suppression des "AndAnd" en trop dans le nom de la méthode
    boolean existsByEmployeIdAndDateDebutAndTypeCongeAndStatutIn(
            @Param("employeId") Long employeId,
            @Param("dateDebut") LocalDate dateDebut,
            @Param("typeConge") String typeConge,
            @Param("statuts") List<String> statuts);

    @Query("SELECT COALESCE(SUM(c.nbjours), 0) FROM Conge c " +
            "WHERE c.employe.id = :employeId " +
            "AND c.statut = 'APPROUVE' " +
            "AND FUNCTION('YEAR', c.dateDebut) = :annee")
    Integer sumJoursConsommesByEmployeAndYear(@Param("employeId") Long employeId,
                                              @Param("annee") Integer annee);
}