package org.example.rhcamunda.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rhcamunda.entity.ParametreGlobal;
import org.example.rhcamunda.repository.ParametreGlobalRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service de gestion des paramètres globaux de l'application (table clé-valeur).
 * Utilisé pour configurer dynamiquement les règles métier sans redéploiement.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ParametreGlobalService {

    private final ParametreGlobalRepository parametreGlobalRepository;

    // =================================================================
    // 🔹 LECTURE
    // =================================================================

    /**
     * Récupère la valeur brute d'un paramètre, ou la valeur par défaut si absent.
     */
    @Transactional(readOnly = true)
    public String getValeur(String cle, String defaultValue) {
        return parametreGlobalRepository.findByCle(cle)
                .map(ParametreGlobal::getValeur)
                .orElse(defaultValue);
    }

    /**
     * Récupère un paramètre sous forme d'entier.
     */
    @Transactional(readOnly = true)
    public int getValeurAsInt(String cle, int defaultValue) {
        Optional<ParametreGlobal> param = parametreGlobalRepository.findByCle(cle);
        if (param.isEmpty()) {
            log.debug("Paramètre '{}' non trouvé, valeur par défaut: {}", cle, defaultValue);
            return defaultValue;
        }
        try {
            return Integer.parseInt(param.get().getValeur().trim());
        } catch (NumberFormatException e) {
            log.warn("Valeur non numérique pour le paramètre '{}': '{}'. Valeur par défaut: {}",
                    cle, param.get().getValeur(), defaultValue);
            return defaultValue;
        }
    }

    /**
     * Récupère un paramètre sous forme de double.
     */
    @Transactional(readOnly = true)
    public double getValeurAsDouble(String cle, double defaultValue) {
        Optional<ParametreGlobal> param = parametreGlobalRepository.findByCle(cle);
        if (param.isEmpty()) {
            log.debug("Paramètre '{}' non trouvé, valeur par défaut: {}", cle, defaultValue);
            return defaultValue;
        }
        try {
            return Double.parseDouble(param.get().getValeur().trim());
        } catch (NumberFormatException e) {
            log.warn("Valeur non numérique pour le paramètre '{}': '{}'. Valeur par défaut: {}",
                    cle, param.get().getValeur(), defaultValue);
            return defaultValue;
        }
    }

    /**
     * Récupère un paramètre sous forme de boolean.
     */
    @Transactional(readOnly = true)
    public boolean getValeurAsBoolean(String cle, boolean defaultValue) {
        Optional<ParametreGlobal> param = parametreGlobalRepository.findByCle(cle);
        if (param.isEmpty()) {
            return defaultValue;
        }
        return Boolean.parseBoolean(param.get().getValeur().trim());
    }

    // =================================================================
    // 🔹 ÉCRITURE (upsert)
    // =================================================================

    /**
     * Crée ou met à jour un paramètre.
     */
    public ParametreGlobal setValeur(String cle, String valeur, String description) {
        ParametreGlobal param = parametreGlobalRepository.findByCle(cle)
                .orElse(ParametreGlobal.builder().cle(cle).build());

        param.setValeur(valeur);
        if (description != null && !description.isBlank()) {
            param.setDescription(description);
        }

        ParametreGlobal saved = parametreGlobalRepository.save(param);
        log.debug("Paramètre '{}' défini à '{}'", cle, valeur);
        return saved;
    }
}
