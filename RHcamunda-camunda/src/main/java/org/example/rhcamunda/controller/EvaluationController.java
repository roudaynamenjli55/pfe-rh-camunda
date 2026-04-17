package org.example.rhcamunda.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rhcamunda.dto.ApiResponse;
import org.example.rhcamunda.dto.evaluation.*;
import org.example.rhcamunda.service.EvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/evaluation")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping("/auto-evaluation")
    @PreAuthorize("hasRole('ROLE_EMPLOYE')")
    public ResponseEntity<ApiResponse<EvaluationDTO>> soumettreAutoEvaluation(
            @RequestBody EvaluationRequestDTO requestDTO,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Requête POST /api/evaluation/auto-evaluation");

        try {
            EvaluationDTO evaluation = evaluationService.soumettreAutoEvaluation(requestDTO, jwt);
            return ResponseEntity.ok(ApiResponse.success("Auto-évaluation soumise avec succès", evaluation));
        } catch (Exception e) {
            log.error("Erreur soumission auto-évaluation", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/valider")
    @PreAuthorize("hasAnyRole('ROLE_CHEF_HIERARCHIQUE', 'ROLE_RH')")
    public ResponseEntity<ApiResponse<EvaluationDTO>> validerEvaluation(
            @RequestBody ValidationEvaluationDTO requestDTO,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Requête POST /api/evaluation/valider");

        try {
            EvaluationDTO evaluation = evaluationService.validerEvaluation(requestDTO, jwt);
            return ResponseEntity.ok(ApiResponse.success("Évaluation validée avec succès", evaluation));
        } catch (Exception e) {
            log.error("Erreur validation évaluation", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/campagne/{campagneId}")
    @PreAuthorize("hasAnyRole('ROLE_EMPLOYE', 'ROLE_CHEF_HIERARCHIQUE', 'ROLE_RH')")
    public ResponseEntity<ApiResponse<List<EvaluationDTO>>> getEvaluationsByCampagne(
            @PathVariable Long campagneId,
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Requête GET /api/evaluation/campagne/{}", campagneId);

        try {
            List<EvaluationDTO> evaluations = evaluationService.getEvaluationsByCampagne(campagneId, jwt);
            return ResponseEntity.ok(ApiResponse.success("Évaluations récupérées", evaluations));
        } catch (Exception e) {
            log.error("Erreur récupération évaluations", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/mes-evaluations")
    @PreAuthorize("hasRole('ROLE_EMPLOYE')")
    public ResponseEntity<ApiResponse<List<EvaluationDTO>>> getMesEvaluations(
            @AuthenticationPrincipal Jwt jwt) {
        log.info("Requête GET /api/evaluation/mes-evaluations");

        try {
            List<EvaluationDTO> evaluations = evaluationService.getMesEvaluations(jwt);
            return ResponseEntity.ok(ApiResponse.success("Évaluations récupérées", evaluations));
        } catch (Exception e) {
            log.error("Erreur récupération évaluations", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/campagne/{campagneId}/score-moyen")
    @PreAuthorize("hasAnyRole('ROLE_RH', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<Double>> getScoreMoyenCampagne(
            @PathVariable Long campagneId) {
        log.info("Requête GET /api/evaluation/campagne/{}/score-moyen", campagneId);

        try {
            Double scoreMoyen = evaluationService.getScoreMoyenCampagne(campagneId);
            return ResponseEntity.ok(ApiResponse.success("Score moyen récupéré", scoreMoyen));
        } catch (Exception e) {
            log.error("Erreur récupération score moyen", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}