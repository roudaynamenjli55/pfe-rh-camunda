package org.example.rhcamunda.service; // ⚠️ Assure-toi que c'est bien 'service' en minuscule

import org.example.rhcamunda.dto.auth.LoginRequest;
import org.example.rhcamunda.dto.auth.LoginResponse;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class KeycloakAuthService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final String KEYCLOAK_URL = "http://localhost:8080";
    private final String REALM = "PFEE-REALM";
    private final String CLIENT_ID = "angular-frontend";

    public LoginResponse authenticate(LoginRequest request) {

        String tokenUrl = KEYCLOAK_URL + "/realms/" + REALM + "/protocol/openid-connect/token";

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "password");
        params.add("client_id", CLIENT_ID);
        params.add("username", request.getUsername());
        params.add("password", request.getPassword());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> httpRequest = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(tokenUrl, httpRequest, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> tokenData = response.getBody();

                // ✅ CORRECTION ICI : Les 5 paramètres correspondent maintenant à ton DTO
                return new LoginResponse(
                        "Authentification réussie",                  // message
                        "SUCCESS",                                   // status
                        (String) tokenData.get("access_token"),      // accessToken
                        (String) tokenData.get("refresh_token"),     // refreshToken
                        ((Number) tokenData.get("expires_in")).intValue() // expiresIn (Cast sécurisé de Number vers Integer)
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Erreur d'authentification: " + e.getMessage());
        }

        throw new RuntimeException("Identifiants invalides ou serveur Keycloak injoignable");
    }
}