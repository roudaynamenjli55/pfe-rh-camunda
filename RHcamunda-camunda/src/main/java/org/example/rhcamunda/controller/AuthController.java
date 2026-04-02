package org.example.rhcamunda.controller;

import org.example.rhcamunda.dto.LoginRequest;
import org.example.rhcamunda.dto.LoginResponse;
import org.example.rhcamunda.service.KeycloakAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    @Autowired
    private KeycloakAuthService keycloakAuthService;

    /**
     * LOGIN - Authentification via username/password
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {

        try {
            LoginResponse response = keycloakAuthService.authenticate(loginRequest);
            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(
                    new LoginResponse("Échec d'authentification: " + e.getMessage(), "ERROR", null, null, null)
            );
        }
    }

    /**
     * Récupérer les informations de l'utilisateur connecté
     * GET /api/auth/me
     */
    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> userInfo = new HashMap<>();

        userInfo.put("username", jwt.getClaim("preferred_username"));
        userInfo.put("email", jwt.getClaim("email"));
        userInfo.put("name", jwt.getClaim("name"));
        userInfo.put("givenName", jwt.getClaim("given_name"));
        userInfo.put("familyName", jwt.getClaim("family_name"));

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null) {
            List<String> roles = (List<String>) realmAccess.get("roles");
            userInfo.put("roles", roles);
        }

        userInfo.put("subject", jwt.getSubject());
        userInfo.put("issuer", jwt.getIssuer());
        userInfo.put("expiresAt", jwt.getExpiresAt());

        return userInfo;
    }

    /**
     * Déconnexion
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        response.setHeader("Clear-Site-Data", "\"cache\", \"cookies\", \"storage\"");

        Map<String, String> message = new HashMap<>();
        message.put("message", "Déconnexion réussie");
        message.put("status", "SUCCESS");

        return ResponseEntity.ok(message);
    }

    /**
     * Vérifier si l'utilisateur est authentifié
     * GET /api/auth/check
     */
    @GetMapping("/check")
    public Map<String, Object> checkAuth(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> response = new HashMap<>();

        if (jwt != null) {
            response.put("authenticated", true);
            response.put("username", jwt.getClaim("preferred_username"));
            response.put("message", "Utilisateur authentifié");
        } else {
            response.put("authenticated", false);
            response.put("message", "Non authentifié");
        }

        return response;
    }

    /**
     * Obtenir les permissions de l'utilisateur
     * GET /api/auth/permissions
     */
    @GetMapping("/permissions")
    public Map<String, Object> getPermissions(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> permissions = new HashMap<>();

        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        List<String> roles = (List<String>) realmAccess.get("roles");

        permissions.put("isAdmin", roles.contains("ROLE_ADMIN"));
        permissions.put("isRH", roles.contains("ROLE_RH"));
        permissions.put("isChef", roles.contains("ROLE_CHEF_HIERARCHIQUE"));
        permissions.put("isEmploye", roles.contains("ROLE_EMPLOYE"));
        permissions.put("roles", roles);

        return permissions;
    }
}