package org.example.rhcamunda.repository;

import org.example.rhcamunda.entity.CampagneEvaluation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CampagneEvaluationRepository extends JpaRepository<CampagneEvaluation, Long> {

    Optional<CampagneEvaluation> findByNom(String nom);

    List<CampagneEvaluation> findByStatut(String statut);

    @Query("SELECT c FROM CampagneEvaluation c WHERE c.dateDebut <= :date AND c.dateFin >= :date AND c.statut = 'ACTIVE'")
    List<CampagneEvaluation> findCampagnesActives(@Param("date") LocalDate date);

    List<CampagneEvaluation> findByAnnee(Long annee);

    boolean existsByNomAndAnnee(String nom, Long annee);
}