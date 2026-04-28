package org.example.rhcamunda.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rhcamunda.dto.evaluation.EvaluationDTO;
import org.example.rhcamunda.dto.evaluation.EvaluationRequestDTO;
import org.example.rhcamunda.dto.evaluation.ValidationEvaluationDTO;
import org.example.rhcamunda.entity.CampagneEvaluation;
import org.example.rhcamunda.entity.Employe;
import org.example.rhcamunda.entity.Evaluation;
import org.example.rhcamunda.repository.CampagneEvaluationRepository;
import org.example.rhcamunda.repository.EmployeRepository;
import org.example.rhcamunda.repository.EvaluationRepository;
import org.example.rhcamunda.util.JwtUtil;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final CampagneEvaluationRepository campagneEvaluationRepository;
    private final EmployeRepository employeRepository;
    private final JwtUtil jwtUtil;
    private final ParametreGlobalService parametreGlobalService;

    @PreAuthorize("hasRole('ROLE_EMPLOYE')")
    @Transactional
    public EvaluationDTO soumettreAutoEvaluation(EvaluationRequestDTO requestDTO, Jwt jwt) {
        log.info("Soumission auto-évaluation pour employé: {}", requestDTO.getMatriculeEmploye());

        String matriculeAuth = jwtUtil.extractMatricule(jwt);

        // Vérifier que l'employé ne peut créer que sa propre évaluation
        if (!matriculeAuth.equals(requestDTO.getMatriculeEmploye())) {
            throw new SecurityException("Vous ne pouvez créer que votre propre évaluation");
        }

        // Vérifier campagne existe et est active
        CampagneEvaluation campagne = campagneEvaluationRepository.findById(requestDTO.getIdCampagne())
                .orElseThrow(() -> new RuntimeException("Campagne non trouvée"));

        if (!"ACTIVE".equals(campagne.getStatut())) {
            throw new IllegalStateException("La campagne n'est pas active");
        }

        if (LocalDate.now().isBefore(campagne.getDateDebut()) || LocalDate.now().isAfter(campagne.getDateFin())) {
            throw new IllegalStateException("Hors période d'évaluation");
        }

        Employe employe = employeRepository.findByMatricule(requestDTO.getMatriculeEmploye())
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));

        // Vérifier si évaluation existe déjà
        if (evaluationRepository.findByEmployeMatriculeAndCampagneId(
                requestDTO.getMatriculeEmploye(), requestDTO.getIdCampagne()).isPresent()) {
            throw new IllegalStateException("Auto-évaluation déjà soumise pour cette campagne");
        }

        Evaluation evaluation = new Evaluation();
        evaluation.setEmploye(employe);
        evaluation.setCampagne(campagne); // ✅ Correction: setCampagne() pas setCampagneEvaluation()
        evaluation.saisirAutoEvaluation(
                requestDTO.getScoreAuto(),
                requestDTO.getCommentaireEmploye(),
                requestDTO.getNombreCongesPris(),
                requestDTO.getNombreAutorisationsPrises()
        );
        evaluation.setDateCreation(LocalDateTime.now()); // ✅ Correction: LocalDateTime

        Evaluation saved = evaluationRepository.save(evaluation);
        log.info("Auto-évaluation soumise: ID={}", saved.getId());

        return mapToDTO(saved);
    }

    @PreAuthorize("hasAnyRole('ROLE_CHEF_HIERARCHIQUE', 'ROLE_RH')")
    @Transactional
    public EvaluationDTO validerEvaluation(ValidationEvaluationDTO requestDTO, Jwt jwt) {
        log.info("Validation évaluation: {}", requestDTO.getEvaluationId());

        Evaluation evaluation = evaluationRepository.findById(requestDTO.getEvaluationId())
                .orElseThrow(() -> new RuntimeException("Évaluation non trouvée"));

        if (!"AUTO_EVALUATION_FAITE".equals(evaluation.getStatut()) &&
                !"EN_COURS_WORKFLOW".equals(evaluation.getStatut())) {
            throw new IllegalStateException("Évaluation déjà traitée ou non prête");
        }

        // ✅ Récupération dynamique des paramètres
        double poidsAuto = parametreGlobalService.getValeurAsDouble("POIDS_AUTO_EVALUATION", 0.3);
        double poidsManager = parametreGlobalService.getValeurAsDouble("POIDS_MANAGER_EVALUATION", 0.7);
        double penaliteConge = parametreGlobalService.getValeurAsDouble("PENALITE_PAR_CONGE", 0.5);
        double penaliteAuto = parametreGlobalService.getValeurAsDouble("PENALITE_PAR_AUTORISATION", 0.2);

        // ✅ Utilise la méthode de l'entity pour évaluer par manager avec paramètres dynamiques
        evaluation.evaluerParManager(requestDTO.getScoreFinal(), requestDTO.getCommentaireManager(), poidsAuto, poidsManager, penaliteConge, penaliteAuto);

        if (requestDTO.isValidee()) {
            evaluation.setStatut("VALIDEE");
        } else {
            evaluation.setStatut("REJETEE");
        }

        evaluation.setDateValidation(LocalDateTime.now()); // ✅ Correction: LocalDateTime

        Evaluation saved = evaluationRepository.save(evaluation);
        log.info("Évaluation validée: ID={}, statut={}", saved.getId(), saved.getStatut());

        return mapToDTO(saved);
    }

    @PreAuthorize("hasAnyRole('ROLE_EMPLOYE', 'ROLE_CHEF_HIERARCHIQUE', 'ROLE_RH')")
    public List<EvaluationDTO> getEvaluationsByCampagne(Long campagneId, Jwt jwt) {
        String matricule = jwtUtil.extractMatricule(jwt);
        List<String> roles = jwtUtil.extractRoles(jwt);

        // Employé ne voit que ses évaluations
        if (roles.contains("ROLE_EMPLOYE")) {
            return evaluationRepository.findByEmployeMatricule(matricule).stream()
                    .filter(e -> e.getCampagne().getId().equals(campagneId)) // ✅ Correction: getCampagne()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }

        // Chef et RH voient toutes les évaluations de la campagne
        return evaluationRepository.findByCampagneId(campagneId).stream() // ✅ Correction: findByCampagneId()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ROLE_EMPLOYE')")
    public List<EvaluationDTO> getMesEvaluations(Jwt jwt) {
        String matricule = jwtUtil.extractMatricule(jwt);
        log.info("Récupération des évaluations pour: {}", matricule);

        return evaluationRepository.findByEmployeMatricule(matricule).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('ROLE_RH', 'ROLE_ADMIN')")
    public Double getScoreMoyenCampagne(Long campagneId) {
        return evaluationRepository.getScoreMoyenByCampagne(campagneId);
    }

    // ✅ Méthode de mapping corrigée
    private EvaluationDTO mapToDTO(Evaluation evaluation) {
        EvaluationDTO dto = new EvaluationDTO();
        dto.setId(evaluation.getId());
        dto.setMatriculeEmploye(evaluation.getEmploye().getMatricule());
        dto.setNomEmploye(evaluation.getEmploye().getNom() + " " + evaluation.getEmploye().getPrenom());
        dto.setIdCampagne(evaluation.getCampagne().getId()); // ✅ Correction: getCampagne()
        dto.setNomCampagne(evaluation.getCampagne().getNom()); // ✅ Correction: getCampagne()
        dto.setScoreAuto(evaluation.getScoreAutoEvaluation()); // ✅ Correction: getScoreAutoEvaluation()
        dto.setNoteValidation(evaluation.getScoreFinal());
        dto.setStatut(evaluation.getStatut());
        dto.setCommentaireEmploye(evaluation.getCommentaireAutoEvaluation()); // ✅ Correction
        dto.setCommentaireManager(evaluation.getCommentaireManager());
        dto.setNoteValidation(evaluation.getScoreFinal()); // ✅ Note = Score final calculé
        return dto;
    }
}