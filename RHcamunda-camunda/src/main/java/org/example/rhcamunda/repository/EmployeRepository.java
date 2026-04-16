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
    // 🔹 RECHERCHE SIMPLE (Finders Spring Data)
    // =================================================================

    Optional<Employe> findByMatricule(String matricule);

    Optional<Employe> findByEmail(String email);

    Optional<Employe> findByEmailAndActifTrue(String email);

    // =================================================================
    // 🔹 FILTRES PAR ENTITÉ ACTIVE
    // =================================================================

    List<Employe> findByActifTrue();

    List<Employe> findByActifFalse();

    Page<Employe> findByActifTrue(Pageable pageable);

    // =================================================================
    // 🔹 FILTRES PAR RELATIONS (Département, Agence, Poste)
    // =================================================================

    // Département
    List<Employe> findByDepartementId(Long departementId);

    List<Employe> findByDepartementIdAndActifTrue(Long departementId);

    // Agence
    List<Employe> findByAgenceId(Long agenceId);

    List<Employe> findByAgenceIdAndActifTrue(Long agenceId);

    // Poste
    List<Employe> findByPosteId(Long posteId);

    List<Employe> findByPosteIdAndActifTrue(Long posteId);

    // =================================================================
    // 🔹 RECHERCHE AVANCÉE (JPQL)
    // =================================================================

    @Query("""
        SELECT e FROM Employe e 
        WHERE e.actif = true 
        AND (LOWER(e.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) 
          OR LOWER(e.prenom) LIKE LOWER(CONCAT('%', :keyword, '%')) 
          OR LOWER(e.matricule) LIKE LOWER(CONCAT('%', :keyword, '%'))
          OR LOWER(e.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY e.nom, e.prenom
        """)
    List<Employe> searchByName(@Param("keyword") String keyword);

    @Query("""
        SELECT e FROM Employe e 
        WHERE e.actif = true 
        AND e.departement.id = :departementId
        """)
    List<Employe> findByDepartementAndActif(@Param("departementId") Long departementId);

    @Query("""
        SELECT e FROM Employe e 
        WHERE e.actif = true 
        AND e.agence.id = :agenceId
        """)
    List<Employe> findByAgenceAndActif(@Param("agenceId") Long agenceId);

    // 🔹 Recherche par hiérarchie (employés sous un manager)
    @Query("""
        SELECT e FROM Employe e 
        WHERE e.actif = true 
        AND e.superieur.id = :managerId
        """)
    List<Employe> findBySuperieurIdAndActifTrue(@Param("managerId") Long managerId);

    // =================================================================
    // 🔹 PAGINATION AVANCÉE
    // =================================================================

    Page<Employe> findAll(Pageable pageable);

    @Query("""
        SELECT e FROM Employe e 
        WHERE e.actif = true 
        ORDER BY e.nom, e.prenom
        """)
    Page<Employe> findActifsPagines(Pageable pageable);

    @Query("""
        SELECT e FROM Employe e 
        WHERE e.departement.id = :departementId 
        AND e.actif = true
        """)
    Page<Employe> findByDepartementIdAndActifTrue(@Param("departementId") Long departementId, Pageable pageable);

    // =================================================================
    // 🔹 VÉRIFICATIONS UNIQUETÉ (pour matricule auto-généré)
    // =================================================================

    boolean existsByMatricule(String matricule);

    boolean existsByEmailAndActifTrue(String email);

    // =================================================================
    // 🔹 COMPTAGES & STATISTIQUES (Dashboard RH)
    // =================================================================

    // Comptages simples
    long countByActifTrue();

    long countByActifFalse();

    long countByDepartementId(Long departementId);

    long countByDepartementIdAndActifTrue(Long departementId);

    long countByAgenceId(Long agenceId);

    long countByAgenceIdAndActifTrue(Long agenceId);

    // Comptages temporels (pour stats d'embauche)
    @Query("""
        SELECT COUNT(e) FROM Employe e 
        WHERE e.actif = true 
        AND e.dateEmbauche BETWEEN :startDate AND :endDate
        """)
    long countByDateEmbaucheBetweenAndActifTrue(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // 🔹 Pour génération matricule : compte tous les employés (actifs + archivés) embauchés dans l'année
    @Query("""
        SELECT COUNT(e) FROM Employe e 
        WHERE e.dateEmbauche BETWEEN :startDate AND :endDate
        """)
    long countByDateEmbaucheBetweenAll(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // =================================================================
    // 🔹 REQUÊTES SPÉCIALES (Export, Dashboard)
    // =================================================================

    @Query("""
        SELECT e FROM Employe e 
        WHERE e.actif = true 
        ORDER BY e.dateEmbauche DESC
        """)
    List<Employe> findTopNouveauxEmployes(@Param("limit") int limit);

    @Query("""
        SELECT e FROM Employe e 
        WHERE e.actif = true 
        AND e.soldeConge < :seuil
        """)
    List<Employe> findBySoldeCongeInferieur(@Param("seuil") Double seuil);

    @Query("""
        SELECT DISTINCT e.departement.id 
        FROM Employe e 
        WHERE e.actif = true 
        AND e.departement IS NOT NULL
        """)
    List<Long> findDistinctDepartementIdsWithActifs();

    @Query("""
        SELECT DISTINCT e.agence.id 
        FROM Employe e 
        WHERE e.actif = true 
        AND e.agence IS NOT NULL
        """)
    List<Long> findDistinctAgenceIdsWithActifs();
}