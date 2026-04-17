package org.example.rhcamunda.security;

import org.example.rhcamunda.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserContext {

    private static final Logger logger = LoggerFactory.getLogger(UserContext.class);

    private static final ThreadLocal<String> currentUserMatricule = new ThreadLocal<>();

    /**
     * Récupère le matricule depuis le contexte de sécurité Keycloak
     */
    public static String getMatriculeCourant() {
        // Vérifier ThreadLocal d'abord (pour Camunda)
        String matricule = currentUserMatricule.get();
        if (matricule != null) {
            return matricule;
        }

        // Fallback sur SecurityContext
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                logger.warn("Tentative d'accès sans authentification");
                throw new IllegalStateException("Utilisateur non authentifié");
            }

            // Extraire depuis JWT (Keycloak)
            if (authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                matricule = jwt.getClaimAsString("matricule");

                if (matricule == null) {
                    matricule = jwt.getClaimAsString("preferred_username");
                }

                if (matricule == null || matricule.trim().isEmpty()) {
                    logger.error("Matricule introuvable dans le token JWT");
                    throw new IllegalStateException("Matricule utilisateur introuvable");
                }

                return matricule;
            }

            logger.error("Principal inconnu: {}", authentication.getPrincipal().getClass());
            throw new IllegalStateException("Type d'authentification non supporté");

        } catch (Exception e) {
            logger.error("Erreur récupération matricule: {}", e.getMessage(), e);
            throw new IllegalStateException("Impossible d'identifier l'utilisateur: " + e.getMessage());
        }
    }

    /**
     * Définir le contexte (pour Camunda/Async)
     */
    public static void setCurrentUser(String matricule) {
        if (matricule == null || matricule.trim().isEmpty()) {
            throw new IllegalArgumentException("Matricule ne peut pas être vide");
        }
        currentUserMatricule.set(matricule);
        logger.debug("Contexte utilisateur défini: {}", matricule);
    }

    /**
     * Nettoyer le contexte
     */
    public static void clear() {
        currentUserMatricule.remove();
    }

    /**
     * Vérifier rôle
     */
    public static boolean hasRole(String role) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) return false;

            return auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_" + role) ||
                            a.getAuthority().equals(role));
        } catch (Exception e) {
            logger.error("Erreur vérification rôle: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Récupérer le JWT complet
     */
    public static Optional<Jwt> getCurrentJwt() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth.getPrincipal() instanceof Jwt) {
                return Optional.of((Jwt) auth.getPrincipal());
            }
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Erreur récupération JWT: {}", e.getMessage());
            return Optional.empty();
        }
    }
}