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

    // =================================================================
    // 🔹 QUOTA: Compter les autorisations par type et par mois
    // =================================================================

    @Query("SELECT COUNT(a) FROM Autorisation a " +
            "WHERE a.employe.id = :employeId " +
            "AND a.type = :type " +
            "AND FUNCTION('DATE_TRUNC', 'month', a.date) = :mois")
    long countByEmployeIdAndTypeAndMonth(@Param("employeId") Long employeId,
                                         @Param("type") String type,
                                         @Param("mois") LocalDate mois);

    // =================================================================
    // 🔹 HISTORIQUE: Lister par employé
    // =================================================================

    List<Autorisation> findByEmployeOrderByDateDesc(Employe employe);

    List<Autorisation> findByEmployeAndDateBetween(Employe employe, LocalDate debut, LocalDate fin);

    List<Autorisation> findByEmployeAndStatutOrderByDateDesc(Employe employe, String statut);

    List<Autorisation> findByEmployeAndTypeOrderByDateDesc(Employe employe, String type);

    // =================================================================
    // 🔹 RECHERCHE: Trouver une autorisation
    // =================================================================

    @Query("SELECT a FROM Autorisation a WHERE a.id = :id AND a.employe.id = :employeId")
    Optional<Autorisation> findByEmployeIdAndId(@Param("employeId") Long employeId,
                                                @Param("id") Long id);

    Optional<Autorisation> findByProcessInstanceId(String processInstanceId);

    boolean existsByEmployeAndDateAndType(Employe employe, LocalDate date, String type);

    // =================================================================
    // 🔹 STATISTIQUES: Pour le dashboard RH
    // =================================================================

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
            "AND FUNCTION('DATE_TRUNC', 'month', a.date) = :mois")
    long countByDepartementAndMonth(@Param("departementId") Long departementId,
                                    @Param("mois") LocalDate mois);

    // =================================================================
    // 🔹 EXPORT: Données pour rapports
    // =================================================================

    @Query("SELECT a FROM Autorisation a " +
            "WHERE a.statut = 'VALIDEE' " +
            "AND FUNCTION('DATE_TRUNC', 'month', a.date) = :mois " +
            "ORDER BY a.date DESC")
    List<Autorisation> findValidatedByMonth(@Param("mois") LocalDate mois);

    List<Autorisation> findByEmploye_AgenceIdOrderByDateDesc(Long agenceId);

    // =================================================================
    // 🔹 NOTIFICATIONS: Demandes en attente
    // =================================================================

    long countByEmployeAndStatut(Employe employe, String statut);

    List<Autorisation> findByEmployeAndStatutOrderByDateAsc(Employe employe, String statut);
}