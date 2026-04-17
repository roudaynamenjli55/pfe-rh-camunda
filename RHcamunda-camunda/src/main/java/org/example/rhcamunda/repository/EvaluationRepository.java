package org.example.rhcamunda.repository;

import org.example.rhcamunda.entity.Evaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluationRepository extends JpaRepository<Evaluation, Long> {

    // ✅ Correction: campagne.id (pas campagneEvaluation.id)
    List<Evaluation> findByCampagneId(Long campagneId);

    // ✅ Correction: employe.matricule + campagne.id
    Optional<Evaluation> findByEmployeMatriculeAndCampagneId(
            @Param("matricule") String matriculeEmploye,
            @Param("campagneId") Long campagneId);

    // ✅ Correction: employe.matricule (navigation through relation)
    List<Evaluation> findByEmployeMatricule(String matriculeEmploye);

    // ✅ Correction: e.campagne.id (pas e.campagneEvaluation.id)
    @Query("SELECT e FROM Evaluation e WHERE e.campagne.id = :campagneId AND e.statut = :statut")
    List<Evaluation> findByCampagneAndStatut(
            @Param("campagneId") Long campagneId,
            @Param("statut") String statut);

    List<Evaluation> findByStatut(String statut);

    // ✅ Correction: e.campagne.id (pas e.campagneEvaluation.id)
    @Query("SELECT AVG(e.scoreFinal) FROM Evaluation e WHERE e.campagne.id = :campagneId")
    Double getScoreMoyenByCampagne(@Param("campagneId") Long campagneId);

    // ✅ Méthode utile pour vérifier l'unicité
    boolean existsByEmployeMatriculeAndCampagneId(String matriculeEmploye, Long campagneId);
}