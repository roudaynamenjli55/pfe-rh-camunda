package org.example.rhcamunda.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rhcamunda.entity.Employe;
import org.example.rhcamunda.repository.EmployeRepository;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUtil {

    private final EmployeRepository employeRepository;

    public String extractMatricule(Jwt jwt) {
        // Validation date d'expiration
        if (jwt.getExpiresAt() != null && jwt.getExpiresAt().isBefore(Instant.now())) {
            log.error("Token JWT expiré pour: {}", jwt.getSubject());
            throw new IllegalStateException("Token expiré");
        }

        String matricule = extractMatriculeFromJwt(jwt);

        if (matricule != null) {
            log.debug("Matricule extrait du token: {}", matricule);

            // Validation employé existe
            Employe employe = employeRepository.findByMatricule(matricule)
                    .orElseThrow(() -> {
                        log.error("Employé non trouvé: {}", matricule);
                        return new RuntimeException("Employé non trouvé: " + matricule);
                    });

            // ✅ Suppression de la vérification getDateDepart() qui n'existe pas
            // Si tu veux vérifier si l'employé est actif, utilise un autre champ
            // Par exemple: if (employe.getArchive() != null && employe.getArchive())
        }

        return matricule;
    }

    private String extractMatriculeFromJwt(Jwt jwt) {
        // Priorité 1: Claim custom "matricule"
        String matricule = jwt.getClaimAsString("matricule");

        // Priorité 2: employeeId
        if (matricule == null) {
            matricule = jwt.getClaimAsString("employeeId");
        }

        // Priorité 3: preferred_username (Keycloak default)
        if (matricule == null) {
            matricule = jwt.getClaimAsString("preferred_username");
        }

        // Validation format matricule (bancaire)
        if (matricule != null && !matricule.matches("^[A-Z0-9]{6,12}$")) {
            log.warn("Format matricule suspect: {}", matricule);
        }

        return matricule;
    }

    public List<String> extractRoles(Jwt jwt) {
        try {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                List<String> roles = (List<String>) realmAccess.get("roles");
                log.debug("Rôles extraits: {}", roles);
                return roles;
            }
        } catch (Exception e) {
            log.error("Erreur extraction rôles: {}", e.getMessage(), e);
        }
        return Collections.emptyList();
    }

    public boolean hasRole(Jwt jwt, String roleName) {
        return extractRoles(jwt).contains(roleName);
    }

    public boolean hasAnyRole(Jwt jwt, String... roles) {
        List<String> userRoles = extractRoles(jwt);
        for (String role : roles) {
            if (userRoles.contains(role)) {
                return true;
            }
        }
        return false;
    }

    public String extractEmail(Jwt jwt) {
        return jwt.getClaimAsString("email");
    }

    public String extractNomComplet(Jwt jwt) {
        String givenName = jwt.getClaimAsString("given_name");
        String familyName = jwt.getClaimAsString("family_name");

        if (givenName != null && familyName != null) {
            return givenName + " " + familyName;
        }
        return jwt.getClaimAsString("name");
    }
}