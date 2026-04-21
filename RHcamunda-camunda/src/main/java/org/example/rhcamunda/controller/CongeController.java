package org.example.rhcamunda.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rhcamunda.dto.conge.CongeRequest;
import org.example.rhcamunda.dto.conge.CongeResponse;
import org.example.rhcamunda.dto.conge.CongeDTO;
import org.example.rhcamunda.service.CongeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conges")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CongeController {

    private final CongeService congeService;

    /**
     * 🔹 CRÉER MA DEMANDE DE CONGÉ
     * ✅ Matricule récupéré automatiquement depuis JWT (pas dans le body)
     * ✅ Validation avec @Valid
     * ✅ Logique déléguée à CongeService
     */
    @PostMapping
    public ResponseEntity<CongeResponse> creerMaDemande(@RequestBody @Valid CongeRequest request) {
        log.info("📝 Nouvelle demande de congé reçue");

        // ✅ Le matricule est récupéré automatiquement depuis le contexte de sécurité
        // Aucune donnée sensible ne vient du body pour l'identité
        CongeResponse response = congeService.creerDemandeConge(request);

        return ResponseEntity.ok(response);
    }

    /**
     * 🔹 OBTENIR MES CONGÉS
     * ✅ Récupération automatique depuis le JWT
     */
    @GetMapping("/my-conges")
    public ResponseEntity<List<CongeDTO>> getMyConges() {
        log.info("📋 Récupération de mes congés");
        return ResponseEntity.ok(congeService.getMyConges());
    }

    /**
     * 🔹 OBTENIR MES CONGÉS EN ATTENTE
     */
    @GetMapping("/my-conges/en-attente")
    public ResponseEntity<List<CongeDTO>> getMyCongesEnAttente() {
        return ResponseEntity.ok(congeService.getMyCongesEnAttente());
    }

    /**
     * 🔹 OBTENIR MON SOLDE DE CONGÉS
     * ✅ Lecture temps réel depuis PostgreSQL
     */
    @GetMapping("/my-solde")
    public ResponseEntity<Double> getMySoldeConge() {
        return ResponseEntity.ok(congeService.getMySoldeConge());
    }

    /**
     * 🔹 OBTENIR LES CONGÉS EN ATTENTE DE VALIDATION (Manager)
     * ✅ Récupère automatiquement les employés sous sa responsabilité
     */
    @GetMapping("/validation/en-attente")
    public ResponseEntity<List<CongeDTO>> getCongesEnAttenteValidation() {
        log.info("🔍 Récupération des congés en attente pour validation");
        return ResponseEntity.ok(congeService.getCongesEnAttenteValidation());
    }

    /**
     * 🔹 APPROUVER UN CONGÉ (Workflow)
     */
    @PostMapping("/{id}/approve")
    public ResponseEntity<Void> approve(@PathVariable Long id) {
        log.info("✅ Approbation congé ID: {}", id);
        congeService.approuverConge(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 🔹 REFUSER UN CONGÉ (Workflow)
     */
    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long id, @RequestParam String motif) {
        log.info("❌ Refus congé ID: {}, motif: {}", id, motif);
        congeService.refuserConge(id, motif);
        return ResponseEntity.ok().build();
    }

    /**
     * 🔹 ANNULER MON CONGÉ
     * ✅ Vérification automatique que c'est bien mon congé
     */
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        log.info("🔄 Annulation congé ID: {}", id);
        congeService.annulerMonConge(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 🔹 OBTENIR UN CONGÉ PAR ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CongeDTO> getConge(@PathVariable Long id) {
        return ResponseEntity.ok(congeService.getCongeById(id));
    }

    /**
     * 🔹 EXPORT EXCEL DE MES CONGÉS
     */
    @GetMapping("/export/excel")
    public ResponseEntity<byte[]> exportMyCongesExcel() {
        // À implémenter avec ExcelExportService
        return ResponseEntity.ok().build();
    }
}