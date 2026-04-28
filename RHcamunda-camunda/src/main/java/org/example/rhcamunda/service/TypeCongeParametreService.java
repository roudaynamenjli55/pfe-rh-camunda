package org.example.rhcamunda.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rhcamunda.entity.TypeCongeParametre;
import org.example.rhcamunda.repository.TypeCongeParametreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service de gestion des types de congés paramétrables.
 * Permet de définir dynamiquement les règles par type (déductible, quota, etc.)
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TypeCongeParametreService {

    private final TypeCongeParametreRepository typeCongeParametreRepository;

    // =================================================================
    // 🔹 LECTURE
    // =================================================================

    /**
     * Récupère un type de congé par son code (ex: "ANNUEL", "MALADIE").
     * Lance une exception si le type n'existe pas.
     */
    @Transactional(readOnly = true)
    public TypeCongeParametre getTypeCode(String code) {
        return typeCongeParametreRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException(
                        "Type de congé inconnu: '" + code + "'. Vérifiez la configuration des types."
                ));
    }

    /**
     * Vérifie si un type de congé existe.
     */
    @Transactional(readOnly = true)
    public boolean existeParCode(String code) {
        return typeCongeParametreRepository.existsByCode(code);
    }

    /**
     * Retourne tous les types de congés actifs.
     */
    @Transactional(readOnly = true)
    public List<TypeCongeParametre> getAllTypesActifs() {
        return typeCongeParametreRepository.findByActifTrue();
    }

    /**
     * Retourne tous les types de congés (actifs et inactifs).
     */
    @Transactional(readOnly = true)
    public List<TypeCongeParametre> getAllTypes() {
        return typeCongeParametreRepository.findAll();
    }

    // =================================================================
    // 🔹 ÉCRITURE (upsert)
    // =================================================================

    /**
     * Crée ou met à jour un type de congé.
     * Utilisé au démarrage par DataInitializer pour les types par défaut.
     */
    public TypeCongeParametre createOrUpdateType(String code, String libelle,
                                                  boolean deductibleSolde, int joursMaxParAn) {
        TypeCongeParametre type = typeCongeParametreRepository.findByCode(code)
                .orElse(TypeCongeParametre.builder().code(code).build());

        type.setLibelle(libelle);
        type.setDeductibleSolde(deductibleSolde);
        type.setJoursMaxParAn(joursMaxParAn);
        type.setActif(true);

        TypeCongeParametre saved = typeCongeParametreRepository.save(type);
        log.debug("Type de congé '{}' créé/mis à jour: déductible={}, max={}j",
                code, deductibleSolde, joursMaxParAn);
        return saved;
    }

    /**
     * Désactive un type de congé (soft delete).
     */
    public void desactiverType(String code) {
        TypeCongeParametre type = getTypeCode(code);
        type.setActif(false);
        typeCongeParametreRepository.save(type);
        log.info("Type de congé '{}' désactivé", code);
    }
}
