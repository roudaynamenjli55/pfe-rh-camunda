package org.example.rhcamunda.repository;

import org.example.rhcamunda.entity.Autorisation;
import org.example.rhcamunda.entity.Employe;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AutorisationRepository extends JpaRepository<Autorisation, Long> {

    // ========================================================================
    // 🔹 QUOTA: Compter les autorisations par type et par mois ✅
    // ========================================================================

    /**
     * Compter autorisations pour vérifier quota (2 par type/mois)
     */
    @Query("SELECT COUNT(a) FROM Autorisation a " +
            "WHERE a.employe.id = :employeId " +
            "AND a.type = :type " +
            "AND YEAR(a.date) = YEAR(:mois) AND MONTH(a.date) = MONTH(:mois)")
    long countByEmployeIdAndTypeAndMonth(@Param("employeId") Long employeId,
                                         @Param("type") String type,
                                         @Param("mois") LocalDate mois);

    /**
     * ⭐ CRITIQUE: Compter autorisations par MATRICULE pour EvaluationService ✅
     * Utilisé dans EvaluationService.calculerNombreAutorisationsCampagne()
     */
    @Query("SELECT COUNT(a) FROM Autorisation a " +
            "WHERE a.employe.matricule = :matricule " +
            "AND a.date BETWEEN :debut AND :fin " +
            "AND a.statut = :statut")
    Integer countByEmployeMatriculeAndDateBetweenAndStatut(
            @Param("matricule") String matricule,
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin,
            @Param("statut") String statut);

    // ========================================================================
    // 🔹 HISTORIQUE: Lister par employé
    // ========================================================================

    List<Autorisation> findByEmployeOrderByDateDesc(Employe employe);

    List<Autorisation> findByEmployeAndDateBetween(Employe employe, LocalDate debut, LocalDate fin);

    List<Autorisation> findByEmployeAndStatutOrderByDateDesc(Employe employe, String statut);

    List<Autorisation> findByEmployeAndTypeOrderByDateDesc(Employe employe, String type);

    /**
     * Historique par matricule (pour EvaluationService)
     */
    @Query("SELECT a FROM Autorisation a " +
            "WHERE a.employe.matricule = :matricule " +
            "AND a.date BETWEEN :debut AND :fin " +
            "AND a.statut = :statut")
    List<Autorisation> findByEmployeMatriculeAndPeriodAndStatut(
            @Param("matricule") String matricule,
            @Param("debut") LocalDate debut,
            @Param("fin") LocalDate fin,
            @Param("statut") String statut);

    // ========================================================================
    // 🔹 RECHERCHE: Trouver une autorisation
    // ========================================================================

    @Query("SELECT a FROM Autorisation a WHERE a.id = :id AND a.employe.id = :employeId")
    Optional<Autorisation> findByEmployeIdAndId(@Param("employeId") Long employeId,
                                                @Param("id") Long id);

    Optional<Autorisation> findByProcessInstanceId(String processInstanceId);

    boolean existsByEmployeAndDateAndType(Employe employe, LocalDate date, String type);

    // ========================================================================
    // 🔹 STATISTIQUES: Pour le dashboard RH
    // ========================================================================

    @Query("SELECT COUNT(a) FROM Autorisation a WHERE a.statut = :statut AND a.date BETWEEN :debut AND :fin")
    long countByStatutAndDateBetween(@Param("statut") String statut,
                                     @Param("debut") LocalDate debut,
                                     @Param("fin") LocalDate fin);

    @Query("SELECT COUNT(a) FROM Autorisation a WHERE a.type = :type AND a.date BETWEEN :debut AND :fin")
    long countByTypeAndDateBetween(@Param("type") String type,
                                   @Param("debut") LocalDate debut,
                                   @Param("fin") LocalDate fin);

    @Query("SELECT COUNT(a) FROM Autorisation a " +
            "WHERE a.employe.departement.id = :departementId " +
            "AND YEAR(a.date) = YEAR(:mois) AND MONTH(a.date) = MONTH(:mois)")
    long countByDepartementAndMonth(@Param("departementId") Long departementId,
                                    @Param("mois") LocalDate mois);

    /**
     * Taux d'utilisation des autorisations par département
     */
    @Query("SELECT a.type, COUNT(a) FROM Autorisation a " +
            "WHERE a.employe.departement.id = :departementId " +
            "AND a.statut = 'VALIDE' " +
            "AND YEAR(a.date) = YEAR(:mois) AND MONTH(a.date) = MONTH(:mois) " +
            "GROUP BY a.type")
    List<Object[]> countByTypeAndDepartementAndMonth(@Param("departementId") Long departementId,
                                                     @Param("mois") LocalDate mois);

    // ========================================================================
    // 🔹 EXPORT: Données pour rapports
    // ========================================================================

    @Query("SELECT a FROM Autorisation a " +
            "WHERE a.statut = 'VALIDEE' " +
            "AND YEAR(a.date) = YEAR(:mois) AND MONTH(a.date) = MONTH(:mois) " +
            "ORDER BY a.date DESC")
    List<Autorisation> findValidatedByMonth(@Param("mois") LocalDate mois);

    List<Autorisation> findByEmploye_AgenceIdOrderByDateDesc(Long agenceId);

    /**
     * Export complet avec détails employé
     */
    @Query("SELECT a FROM Autorisation a " +
            "JOIN FETCH a.employe e " +
            "WHERE a.statut = 'VALIDEE' " +
            "AND a.date BETWEEN :debut AND :fin " +
            "ORDER BY a.date DESC")
    List<Autorisation> findValidatedWithEmployeDetails(@Param("debut") LocalDate debut,
                                                       @Param("fin") LocalDate fin);

    // ========================================================================
    // 🔹 NOTIFICATIONS: Demandes en attente
    // ========================================================================

    long countByEmployeAndStatut(Employe employe, String statut);

    List<Autorisation> findByEmployeAndStatutOrderByDateAsc(Employe employe, String statut);

    /**
     * Demandes en attente pour un manager (ses subordonnés)
     */
    @Query("SELECT a FROM Autorisation a " +
            "WHERE a.employe.chefHierarchique.matricule = :matriculeChef " +
            "AND a.statut = 'EN_ATTENTE'")
    List<Autorisation> findPendingForManager(@Param("matriculeChef") String matriculeChef);

    // ========================================================================
    // 🔹 VALIDATION BUSINESS: Vérifications avant création
    // ========================================================================

    /**
     * Vérifier si quota mensuel est atteint (2 autorisations max par type/mois)
     */
    @Query("SELECT COUNT(a) FROM Autorisation a " +
            "WHERE a.employe.matricule = :matricule " +
            "AND a.type = :type " +
            "AND a.statut IN ('VALIDEE', 'EN_ATTENTE') " +
            "AND YEAR(a.date) = YEAR(:dateReference) AND MONTH(a.date) = MONTH(:dateReference)")
    long countQuotaByMatriculeAndTypeAndMonth(@Param("matricule") String matricule,
                                              @Param("type") String type,
                                              @Param("dateReference") LocalDate dateReference);

    /**
     * Vérifier doublon: même employé, même date, même type, statut actif
     */
    boolean existsByEmployeMatriculeAndDateAndTypeAndStatutIn(
            @Param("matricule") String matricule,
            @Param("date") LocalDate date,
            @Param("type") String type,
            @Param("statuts") List<String> statuts);
}