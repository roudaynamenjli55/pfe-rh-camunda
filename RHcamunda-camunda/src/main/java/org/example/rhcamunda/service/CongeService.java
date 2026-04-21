package org.example.rhcamunda.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.example.rhcamunda.dto.conge.CongeDTO;
import org.example.rhcamunda.dto.conge.CongeRequest;
import org.example.rhcamunda.dto.conge.CongeResponse;
import org.example.rhcamunda.entity.Conge;
import org.example.rhcamunda.entity.Employe;
import org.example.rhcamunda.repository.CongeRepository;
import org.example.rhcamunda.repository.EmployeRepository;
import org.example.rhcamunda.util.UserContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CongeService {

    private final CongeRepository congeRepository;
    private final EmployeRepository employeRepository;
    private final RuntimeService runtimeService;
    private final NotificationService notificationService;
    private final UserContext userContext;

    /**
     * Créer une demande de congé - 100% Dynamique
     */
    public CongeResponse creerDemandeConge(CongeRequest request) {
        String matriculeEmploye = userContext.getCurrentUserMatricule();
        log.info("Création demande congé pour: {}", matriculeEmploye);

        Employe employe = employeRepository.findByMatricule(matriculeEmploye)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé: " + matriculeEmploye));

        // Calcul dynamique des jours
        long nbjours = ChronoUnit.DAYS.between(request.getDateDebut(), request.getDateFin()) + 1;

        // Vérification solde
        if (employe.getSoldeConge() < nbjours) {
            throw new RuntimeException(
                    String.format("Solde insuffisant. Disponible: %d, Demandé: %d",
                            employe.getSoldeConge(), nbjours)
            );
        }

        // Vérification doublons
        boolean existeDeja = congeRepository.existsByEmployeIdAndDateDebutAndTypeCongeAndStatutIn(
                employe.getId(),
                request.getDateDebut(),
                request.getTypeConge(),
                List.of("EN_ATTENTE", "APPROUVE")
        );
        if (existeDeja) {
            throw new RuntimeException("Demande déjà existante pour cette date");
        }

        // Création entité
        Conge conge = Conge.builder()
                .processInstanceId(UUID.randomUUID().toString())
                .typeConge(request.getTypeConge())
                .dateDebut(request.getDateDebut())
                .dateFin(request.getDateFin())
                .nbjours((int) nbjours)
                .motif(request.getMotif())
                .statut(Conge.StatutConge.EN_ATTENTE)
                .employe(employe)
                .remplacant(request.getRemplacantId() != null ?
                        employeRepository.findByMatricule(request.getRemplacantId()).orElse(null) : null)
                .pieceJointe(request.getPieceJointe() != null && request.getPieceJointe())
                .build();

        congeRepository.save(conge);
        log.info("Demande créée ID: {} pour: {}", conge.getId(), matriculeEmploye);

        // Variables workflow Camunda
        Map<String, Object> variables = new HashMap<>();
        variables.put("congeId", conge.getId());
        variables.put("matriculeEmploye", employe.getMatricule());
        variables.put("nomEmploye", employe.getNom() + " " + employe.getPrenom());
        variables.put("typeConge", conge.getTypeConge());
        variables.put("dateDebut", conge.getDateDebut());
        variables.put("dateFin", conge.getDateFin());
        variables.put("nbjours", conge.getNbjours());
        variables.put("motif", conge.getMotif());

        if (employe.getChefHierarchique() != null) {
            variables.put("matriculeManager", employe.getChefHierarchique().getMatricule());
        }

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                "conge-request-process",
                conge.getProcessInstanceId(),
                variables
        );

        conge.setProcessInstanceId(processInstance.getProcessInstanceId());
        congeRepository.save(conge);

        // Notification
        notificationService.sendNotificationToUser(
                matriculeEmploye,
                "Demande de congé soumise avec succès"
        );

        return CongeResponse.builder()
                .id(conge.getId())
                .processInstanceId(conge.getProcessInstanceId())
                .employeMatricule(employe.getMatricule())
                .employeNom(employe.getNom() + " " + employe.getPrenom())
                .typeConge(conge.getTypeConge())
                .dateDebut(conge.getDateDebut())
                .dateFin(conge.getDateFin())
                .nbjours(conge.getNbjours())
                .statut(conge.getStatut().name())
                .dateCreation(conge.getDateCreation())
                .message("Demande créée avec succès")
                .nextStep("En attente de validation par " +
                        (employe.getChefHierarchique() != null ? employe.getChefHierarchique().getNom() : "RH"))
                .build();
    }

    /**
     * Obtenir MES congés - 100% Dynamique
     */
    @Transactional(readOnly = true)
    public List<CongeDTO> getMyConges() {
        String matriculeEmploye = userContext.getCurrentUserMatricule();
        log.info("Récupération congés pour: {}", matriculeEmploye);

        Employe employe = employeRepository.findByMatricule(matriculeEmploye)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé: " + matriculeEmploye));

        return congeRepository.findByEmployeIdOrderByDateDebutDesc(employe.getId()).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir MES congés en attente
     */
    @Transactional(readOnly = true)
    public List<CongeDTO> getMyCongesEnAttente() {
        String matriculeEmploye = userContext.getCurrentUserMatricule();
        Employe employe = employeRepository.findByMatricule(matriculeEmploye)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));

        return congeRepository.findByEmployeIdAndStatut(employe.getId(), "EN_ATTENTE").stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir congés en attente de validation (Manager)
     */
    @Transactional(readOnly = true)
    public List<CongeDTO> getCongesEnAttenteValidation() {
        String matriculeManager = userContext.getCurrentUserMatricule();
        log.info("Récupération congés en attente pour manager: {}", matriculeManager);

        return congeRepository.findByChefMatriculeAndStatut(matriculeManager, "EN_ATTENTE").stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtenir MON solde de congés
     */
    @Transactional(readOnly = true)
    public Double getMySoldeConge() {
        String matriculeEmploye = userContext.getCurrentUserMatricule();
        Employe employe = employeRepository.findByMatricule(matriculeEmploye)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé"));
        return employe.getSoldeConge();
    }

    /**
     * Approuver un congé
     */
    public void approuverConge(Long congeId) {
        log.info("Approbation congé ID: {}", congeId);

        Conge conge = congeRepository.findById(congeId)
                .orElseThrow(() -> new RuntimeException("Congé non trouvé"));

        conge.approuver();

        Employe employe = conge.getEmploye();
        employeRepository.save(employe); // Solde déjà déduit dans approuver()

        congeRepository.save(conge);

        notificationService.sendNotificationToUser(
                employe.getMatricule(),
                "Votre demande de congé a été approuvée"
        );
        log.info("Congé {} approuvé", congeId);
    }

    /**
     * Refuser un congé
     */
    public void refuserConge(Long congeId, String motifRefus) {
        log.info("Refus congé ID: {}. Motif: {}", congeId, motifRefus);

        Conge conge = congeRepository.findById(congeId)
                .orElseThrow(() -> new RuntimeException("Congé non trouvé"));

        conge.refuser(motifRefus);
        congeRepository.save(conge);

        notificationService.sendNotificationToUser(
                conge.getEmploye().getMatricule(),
                "Votre demande de congé a été refusée: " + motifRefus
        );
    }

    /**
     * Annuler mon congé
     */
    public void annulerMonConge(Long congeId) {
        String matriculeEmploye = userContext.getCurrentUserMatricule();
        log.info("Annulation congé ID: {} par {}", congeId, matriculeEmploye);

        Conge conge = congeRepository.findById(congeId)
                .orElseThrow(() -> new RuntimeException("Congé non trouvé"));

        if (!conge.getEmploye().getMatricule().equals(matriculeEmploye)) {
            throw new RuntimeException("Non autorisé à annuler ce congé");
        }

        conge.annuler();
        congeRepository.save(conge);

        notificationService.sendNotificationToUser(
                matriculeEmploye,
                "Votre demande de congé a été annulée"
        );
    }

    /**
     * Obtenir un congé par ID
     */
    @Transactional(readOnly = true)
    public CongeDTO getCongeById(Long id) {
        Conge conge = congeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Congé non trouvé"));
        return mapToDTO(conge);
    }

    /**
     * Mapper Entity -> DTO (complet)
     */
    private CongeDTO mapToDTO(Conge conge) {
        Employe employe = conge.getEmploye();
        Employe remplacant = conge.getRemplacant();

        return CongeDTO.builder()
                .id(conge.getId())
                .processInstanceId(conge.getProcessInstanceId())

                // Employé
                .employeId(employe != null ? employe.getId() : null)
                .employeMatricule(employe != null ? employe.getMatricule() : null)
                .employeNom(employe != null ? employe.getNom() : null)
                .employePrenom(employe != null ? employe.getPrenom() : null)

                // Congé
                .typeConge(conge.getTypeConge())
                .dateDebut(conge.getDateDebut())
                .dateFin(conge.getDateFin())
                .nbjours(conge.getNbjours())
                .statut(conge.getStatut() != null ? conge.getStatut().name() : null)
                .motif(conge.getMotif())

                // Remplaçant
                .remplacantId(remplacant != null ? remplacant.getId() : null)
                .remplacantNom(remplacant != null ? remplacant.getNom() + " " + remplacant.getPrenom() : null)

                // Validation
                .validations(conge.getValidations())
                .pieceJointe(conge.getPieceJointe())
                .dateCreation(conge.getDateCreation())
                .dateValidation(conge.getDateValidation())

                // Calculé
                .soldeRestant(employe != null && employe.getSoldeConge() != null
                        ? employe.getSoldeConge()
                        : null)
                .build();
    }
}