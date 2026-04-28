package org.example.rhcamunda.Config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire global des exceptions – Retourne du JSON structuré pour Angular.
 * Format compatible avec les HttpInterceptors Angular.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 400 – Erreurs de règles métier (RuntimeException)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        return buildError(HttpStatus.BAD_REQUEST, "Erreur métier", ex.getMessage(), request.getRequestURI());
    }

    /**
     * 400 – Erreurs de validation des formulaires (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> validationErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            validationErrors.put(fieldName, error.getDefaultMessage());
        });

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", 400);
        body.put("error", "Erreur de validation");
        body.put("message", "Certains champs sont invalides.");
        body.put("validationErrors", validationErrors);
        body.put("path", request.getRequestURI());

        return ResponseEntity.badRequest().body(body);
    }

    /**
     * 401 – Non authentifié
     */
    @ExceptionHandler({AuthenticationException.class, AuthenticationCredentialsNotFoundException.class})
    public ResponseEntity<Map<String, Object>> handleAuthenticationError(Exception ex, HttpServletRequest request) {
        return buildError(HttpStatus.UNAUTHORIZED, "Non authentifié", "Authentification requise. Veuillez vous connecter.", request.getRequestURI());
    }

    /**
     * 403 – Accès refusé
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedError(AccessDeniedException ex, HttpServletRequest request) {
        return buildError(HttpStatus.FORBIDDEN, "Accès refusé", "Vous n'avez pas les permissions nécessaires.", request.getRequestURI());
    }

    /**
     * 500 – Erreur interne générale
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericError(Exception ex, HttpServletRequest request) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Erreur interne", "Une erreur inattendue s'est produite.", request.getRequestURI());
    }

    // =====================================================================
    // MÉTHODE UTILITAIRE
    // =====================================================================
    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String error, String message, String path) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("path", path);
        return ResponseEntity.status(status).body(body);
    }
}