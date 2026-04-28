package org.example.rhcamunda.repository;

import org.example.rhcamunda.entity.TypeCongeParametre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TypeCongeParametreRepository extends JpaRepository<TypeCongeParametre, Long> {

    Optional<TypeCongeParametre> findByCode(String code);

    boolean existsByCode(String code);

    List<TypeCongeParametre> findByActifTrue();
}
