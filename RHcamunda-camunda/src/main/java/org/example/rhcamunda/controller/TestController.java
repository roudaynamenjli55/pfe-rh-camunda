package org.example.rhcamunda.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    /**
     * Endpoint PUBLIC - Pas besoin d'authentification
     */
    @GetMapping("/public")
    public Map<String, String> publicEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "✅ This is PUBLIC - No auth required");
        response.put("status", "SUCCESS");
        return response;
    }

    /**
     * Endpoint PROTÉGÉ - Nécessite authentification
     */
    @GetMapping("/secured")
    public Map<String, String> securedEndpoint() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "✅ This is SECURED - Auth required");
        response.put("status", "SUCCESS");
        return response;
    }
}