package org.example.rhcamunda.repository;

import org.example.rhcamunda.entity.Conge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CongeRepository extends JpaRepository<Conge, Long> {

    // Trouver toutes les demandes d'un employé
    List<Conge> findByEmployeId(Long employeId);

    // TROUVER UNE DEMANDE PAR L'ID DU PROCESSUS CAMUNDA (TRÈS IMPORTANT!)
    Optional<Conge> findByProcessInstanceId(String processInstanceId);

    // Trouver les demandes en attente
    List<Conge> findByStatut(String statut);

    // Trouver les demandes validées
    List<Conge> findByStatutAndEmployeId(String statut, Long employeId);
}