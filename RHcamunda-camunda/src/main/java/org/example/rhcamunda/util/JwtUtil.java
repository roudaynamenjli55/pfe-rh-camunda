package org.example.rhcamunda.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rhcamunda.entity.Employe;
import org.example.rhcamunda.repository.EmployeRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Utilitaire pour gérer l'extraction des informations depuis le Token JWT (Keycloak).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtil {

    private final EmployeRepository employeRepository;

    /**
     * Extrait le matricule de l'employé connecté depuis le JWT.
     * Essaie plusieurs champs possibles pour maximiser la compatibilité.
     *
     * @param jwt Le token JWT
     * @return Le matricule de l'employé
     */
    public String extractMatricule(Jwt jwt) {
        String matricule = extractMatriculeFromJwt(jwt);

        if (matricule != null) {
            log.debug("Matricule extrait du token: {}", matricule);
            // On vérifie que l'employé existe bien en base
            final String finalMatricule = matricule; // ✅ Variable finale pour le lambda
            employeRepository.findByMatricule(finalMatricule)
                    .orElseThrow(() -> new RuntimeException(
                            "Employé non trouvé avec matricule: " + finalMatricule +
                                    ". Vérifiez que l'utilisateur existe dans la table 'employes'."
                    ));
        }

        return matricule;
    }

    /**
     * Méthode helper pour extraire le matricule du JWT
     */
    private String extractMatriculeFromJwt(Jwt jwt) {
        // Priorité 1: Chercher claim "matricule" (custom claim)
        String matricule = jwt.getClaimAsString("matricule");

        // Priorité 2: Chercher dans "employeeId"
        if (matricule == null) {
            matricule = jwt.getClaimAsString("employeeId");
        }

        // Priorité 3: Fallback sur "preferred_username" (souvent utilisé par Keycloak par défaut)
        if (matricule == null) {
            matricule = jwt.getClaimAsString("preferred_username");
        }

        return matricule;
    }

    /**
     * Extrait la liste des rôles du Realm Keycloak.
     *
     * @param jwt Le token JWT
     * @return Liste des rôles
     */
    public List<String> extractRoles(Jwt jwt) {
        try {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                List<String> roles = (List<String>) realmAccess.get("roles");
                log.debug("Rôles extraits: {}", roles);
                return roles;
            }
        } catch (Exception e) {
            log.warn("Erreur lors de l'extraction des rôles: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * Vérifie si l'utilisateur possède un rôle spécifique.
     *
     * @param jwt Le token JWT
     * @param roleName Le nom du rôle à vérifier (ex: "RH", "MANAGER")
     * @return true si l'utilisateur a le rôle
     */
    public boolean hasRole(Jwt jwt, String roleName) {
        List<String> roles = extractRoles(jwt);
        return roles.contains(roleName);
    }
}