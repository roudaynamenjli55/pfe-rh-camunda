package org.example.rhcamunda.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rhcamunda.dto.ApiResponse;
import org.example.rhcamunda.dto.evaluation.CampagneEvaluationDTO;
import org.example.rhcamunda.dto.evaluation.CampagneEvaluationRequestDTO;
import org.example.rhcamunda.service.CampagneEvaluationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/campagne-evaluation")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CampagneEvaluationController {

    private final CampagneEvaluationService campagneEvaluationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_RH', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<CampagneEvaluationDTO>> creerCampagne(
            @RequestBody CampagneEvaluationRequestDTO requestDTO) {
        log.info("Requête POST /api/campagne-evaluation");

        try {
            CampagneEvaluationDTO campagne = campagneEvaluationService.creerCampagne(requestDTO);
            return ResponseEntity.ok(ApiResponse.success("Campagne créée avec succès", campagne));
        } catch (Exception e) {
            log.error("Erreur création campagne", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_RH', 'ROLE_ADMIN', 'ROLE_CHEF_HIERARCHIQUE')")
    public ResponseEntity<ApiResponse<List<CampagneEvaluationDTO>>> getAllCampagnes() {
        log.info("Requête GET /api/campagne-evaluation");

        try {
            List<CampagneEvaluationDTO> campagnes = campagneEvaluationService.getAllCampagnes();
            return ResponseEntity.ok(ApiResponse.success("Campagnes récupérées", campagnes));
        } catch (Exception e) {
            log.error("Erreur récupération campagnes", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/actives")
    @PreAuthorize("hasAnyRole('ROLE_RH', 'ROLE_ADMIN', 'ROLE_CHEF_HIERARCHIQUE', 'ROLE_EMPLOYE')")
    public ResponseEntity<ApiResponse<List<CampagneEvaluationDTO>>> getCampagnesActives() {
        log.info("Requête GET /api/campagne-evaluation/actives");

        try {
            List<CampagneEvaluationDTO> campagnes = campagneEvaluationService.getCampagnesActives();
            return ResponseEntity.ok(ApiResponse.success("Campagnes actives récupérées", campagnes));
        } catch (Exception e) {
            log.error("Erreur récupération campagnes actives", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @PostMapping("/{id}/cloturer")
    @PreAuthorize("hasAnyRole('ROLE_RH', 'ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<CampagneEvaluationDTO>> cloturerCampagne(
            @PathVariable Long id) {
        log.info("Requête POST /api/campagne-evaluation/{}/cloturer", id);

        try {
            CampagneEvaluationDTO campagne = campagneEvaluationService.cloturerCampagne(id);
            return ResponseEntity.ok(ApiResponse.success("Campagne clôturée avec succès", campagne));
        } catch (Exception e) {
            log.error("Erreur clôture campagne", e);
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}