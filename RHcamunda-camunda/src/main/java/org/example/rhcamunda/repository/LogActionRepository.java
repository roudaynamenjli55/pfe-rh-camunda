package org.example.rhcamunda.repository;

import org.example.rhcamunda.entity.LogAction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LogActionRepository extends JpaRepository<LogAction, Long> {

    // ✅ Recherche par matricule utilisateur (dynamique)
    Page<LogAction> findByUtilisateurMatriculeOrderByTimestampDesc(
            @Param("matricule") String matricule,
            Pageable pageable
    );

    // ✅ Recherche par entité
    Page<LogAction> findByEntiteOrderByTimestampDesc(
            @Param("entite") String entite,
            Pageable pageable
    );

    // ✅ Recherche par action
    Page<LogAction> findByActionOrderByTimestampDesc(
            @Param("action") LogAction.ActionType action,
            Pageable pageable
    );

    // ✅ Recherche par période
    Page<LogAction> findByTimestampBetweenOrderByTimestampDesc(
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin,
            Pageable pageable
    );

    // ✅ Recherche combinée (matricule + période)
    Page<LogAction> findByUtilisateurMatriculeAndTimestampBetweenOrderByTimestampDesc(
            @Param("matricule") String matricule,
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin,
            Pageable pageable
    );

    // ✅ Logs critiques (DELETE, LOGIN, LOGOUT)
    @Query("""
        SELECT l FROM LogAction l 
        WHERE l.action IN ('DELETE', 'LOGIN', 'LOGOUT')
        ORDER BY l.timestamp DESC
        """)
    Page<LogAction> findCriticalActions(Pageable pageable);

    // ✅ Statistiques par action (pour dashboard)
    @Query("""
        SELECT l.action, COUNT(l) 
        FROM LogAction l 
        WHERE l.timestamp >= :debut 
        GROUP BY l.action 
        ORDER BY COUNT(l) DESC
        """)
    List<Object[]> countActionsByType(
            @Param("debut") LocalDateTime debut
    );

    // ✅ Top utilisateurs (plus actifs)
    @Query("""
        SELECT l.utilisateurMatricule, l.utilisateurNom, COUNT(l) 
        FROM LogAction l 
        WHERE l.timestamp >= :debut 
        GROUP BY l.utilisateurMatricule, l.utilisateurNom 
        ORDER BY COUNT(l) DESC
        """)
    List<Object[]> topActiveUsers(
            @Param("debut") LocalDateTime debut,
            Pageable pageable
    );

    // ✅ Taux d'erreur par utilisateur
    @Query("""
        SELECT l.utilisateurMatricule, 
               COUNT(CASE WHEN l.success = false THEN 1 END) * 100.0 / COUNT(*) 
        FROM LogAction l 
        WHERE l.timestamp >= :debut 
        GROUP BY l.utilisateurMatricule
        """)
    List<Object[]> errorRateByUser(
            @Param("debut") LocalDateTime debut
    );

    // ✅ Audit trail d'une entité spécifique
    List<LogAction> findByEntiteAndEntiteIdOrderByTimestampDesc(
            @Param("entite") String entite,
            @Param("entiteId") Long entiteId
    );

    // ✅ Nombre de logs aujourd'hui
    long countByTimestampAfter(LocalDateTime debutJour);
}