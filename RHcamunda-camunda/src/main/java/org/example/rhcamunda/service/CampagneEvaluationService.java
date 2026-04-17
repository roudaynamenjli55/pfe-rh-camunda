package org.example.rhcamunda.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rhcamunda.dto.evaluation.CampagneEvaluationDTO;
import org.example.rhcamunda.dto.evaluation.CampagneEvaluationRequestDTO;
import org.example.rhcamunda.entity.CampagneEvaluation;
import org.example.rhcamunda.repository.CampagneEvaluationRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampagneEvaluationService {

    private final CampagneEvaluationRepository campagneEvaluationRepository;

    @PreAuthorize("hasAnyRole('ROLE_RH', 'ROLE_ADMIN')")
    @Transactional
    public CampagneEvaluationDTO creerCampagne(CampagneEvaluationRequestDTO requestDTO) {
        log.info("Création campagne d'évaluation: {}", requestDTO.getNom());

        if (requestDTO.getDateDebut().isAfter(requestDTO.getDateFin())) {
            throw new IllegalArgumentException("La date de début doit être avant la date de fin");
        }

        if (campagneEvaluationRepository.existsByNomAndAnnee(requestDTO.getNom(), requestDTO.getAnnee())) {
            throw new IllegalArgumentException("Une campagne avec ce nom existe déjà pour cette année");
        }

        CampagneEvaluation campagne = new CampagneEvaluation();
        campagne.setNom(requestDTO.getNom());
        campagne.setDescription(requestDTO.getDescription());
        campagne.setDateDebut(requestDTO.getDateDebut());
        campagne.setDateFin(requestDTO.getDateFin());

        // ✅ Assignation directe - Long vers Long
        campagne.setAnnee(requestDTO.getAnnee());

        campagne.setStatut("ACTIVE");

        CampagneEvaluation saved = campagneEvaluationRepository.save(campagne);
        log.info("Campagne créée avec succès: ID={}", saved.getId());

        return mapToDTO(saved);
    }

    @PreAuthorize("hasAnyRole('ROLE_RH', 'ROLE_ADMIN', 'ROLE_CHEF_HIERARCHIQUE')")
    public List<CampagneEvaluationDTO> getAllCampagnes() {
        log.info("Récupération de toutes les campagnes");
        return campagneEvaluationRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('ROLE_RH', 'ROLE_ADMIN', 'ROLE_CHEF_HIERARCHIQUE', 'ROLE_EMPLOYE')")
    public List<CampagneEvaluationDTO> getCampagnesActives() {
        log.info("Récupération des campagnes actives");
        return campagneEvaluationRepository.findCampagnesActives(LocalDate.now()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @PreAuthorize("hasAnyRole('ROLE_RH', 'ROLE_ADMIN')")
    @Transactional
    public CampagneEvaluationDTO cloturerCampagne(Long id) {
        log.info("Clôture de la campagne: {}", id);

        CampagneEvaluation campagne = campagneEvaluationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campagne non trouvée"));

        campagne.setStatut("CLOTUREE");
        CampagneEvaluation saved = campagneEvaluationRepository.save(campagne);

        log.info("Campagne clôturée: {}", id);
        return mapToDTO(saved);
    }

    private CampagneEvaluationDTO mapToDTO(CampagneEvaluation campagne) {
        CampagneEvaluationDTO dto = new CampagneEvaluationDTO();
        dto.setId(campagne.getId());
        dto.setNom(campagne.getNom());
        dto.setDescription(campagne.getDescription());
        dto.setDateDebut(campagne.getDateDebut());
        dto.setDateFin(campagne.getDateFin());
        dto.setStatut(campagne.getStatut());

        // ✅ Assignation directe - Long vers Long (pas de cast nécessaire)
        dto.setAnnee(campagne.getAnnee());

        return dto;
    }
}