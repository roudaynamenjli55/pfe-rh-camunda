package org.example.rhcamunda.repository;

import org.example.rhcamunda.entity.ParametreGlobal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ParametreGlobalRepository extends JpaRepository<ParametreGlobal, Long> {

    Optional<ParametreGlobal> findByCle(String cle);

    boolean existsByCle(String cle);
}
