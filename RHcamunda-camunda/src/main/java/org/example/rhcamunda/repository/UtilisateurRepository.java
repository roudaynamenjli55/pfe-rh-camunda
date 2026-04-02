package org.example.rhcamunda.repository;

import org.example.rhcamunda.entity.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UtilisateurRepository extends JpaRepository<Utilisateur, Long> {

    Optional<Utilisateur> findByLogin(String login);

    Optional<Utilisateur> findByEmail(String email);

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);

    List<Utilisateur> findByEnabledTrue();
}