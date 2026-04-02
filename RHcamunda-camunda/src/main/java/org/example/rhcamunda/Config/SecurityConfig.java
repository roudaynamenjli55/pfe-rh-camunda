package org.example.rhcamunda.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // ==================== CORS ====================
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ==================== CSRF ====================
                .csrf(csrf -> csrf.disable())

                // ==================== SESSION STATELESS ====================
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // ==================== HEADERS ====================
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable()) // Pour H2 Console
                )

                // ==================== AUTHORIZATION ====================
                .authorizeHttpRequests(auth -> auth
                        // --- 🔓 ENDPOINTS PUBLICS (NO AUTH REQUIRED) ---
                        .requestMatchers("/api/auth/**").permitAll()        // ← LOGIN, CHECK, etc.
                        .requestMatchers("/api/test/public").permitAll()
                        .requestMatchers("/api/public/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/actuator/info").permitAll()
                        .requestMatchers("/swagger-ui/**").permitAll()
                        .requestMatchers("/v3/api-docs/**").permitAll()
                        .requestMatchers("/error").permitAll()

                        // --- 🎭 CAMUNDA (pour dev) ---
                        .requestMatchers("/camunda/**").permitAll()
                        .requestMatchers("/engine-rest/**").permitAll()

                        // --- 🔐 ENDPOINTS PROTÉGÉS PAR RÔLE ---

                        // ADMIN uniquement
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // RH ou ADMIN
                        .requestMatchers("/api/rh/**").hasAnyRole("RH", "ADMIN")

                        // CHEF_HIERARCHIQUE, RH ou ADMIN
                        .requestMatchers("/api/chef/**").hasAnyRole("CHEF_HIERARCHIQUE", "RH", "ADMIN")

                        // EMPLOYE ou supérieur
                        .requestMatchers("/api/employe/**").hasAnyRole("EMPLOYE", "CHEF_HIERARCHIQUE", "RH", "ADMIN")

                        // --- 🔒 TOUT LE RESTE REQUIERT AUTHENTIFICATION ---
                        .anyRequest().authenticated()
                )

                // ==================== OAUTH2 RESOURCE SERVER (KEYCLOAK) ====================
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
                );

        return http.build();
    }

    // ==================== CORS CONFIGURATION ====================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Autoriser les origins (frontend Angular + backend)
        configuration.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://localhost:8081",
                "http://127.0.0.1:4200",
                "http://127.0.0.1:8081"
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    // ==================== JWT AUTHENTICATION CONVERTER ====================
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }

    // ==================== KEYCLOAK ROLE CONVERTER ====================
    @SuppressWarnings("unchecked")
    static class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            List<GrantedAuthority> authorities = new ArrayList<>();

            // 1️⃣ Extraire les rôles de realm_access
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                List<String> roles = (List<String>) realmAccess.get("roles");
                for (String role : roles) {
                    String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();
                    authorities.add(new SimpleGrantedAuthority(roleName));
                }
            }

            // 2️⃣ Extraire les rôles du client (resource_access)
            Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
            if (resourceAccess != null) {
                Map<String, Object> clientAccess = (Map<String, Object>) resourceAccess.get("spring-boot-client");
                if (clientAccess != null && clientAccess.containsKey("roles")) {
                    List<String> roles = (List<String>) clientAccess.get("roles");
                    for (String role : roles) {
                        String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role.toUpperCase();
                        authorities.add(new SimpleGrantedAuthority(roleName));
                    }
                }
            }

            return authorities;
        }
    }
}