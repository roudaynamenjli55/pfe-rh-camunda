package org.example.rhcamunda.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rhcamunda.dto.autorisation.AutorisationDto;
import org.example.rhcamunda.dto.autorisation.AutorisationRequestDto;
import org.example.rhcamunda.entity.Autorisation;
import org.example.rhcamunda.entity.Employe;
import org.example.rhcamunda.repository.AutorisationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutorisationService {

    private final AutorisationRepository autorisationRepo;
    private final EmployeService employeService;
    private final ParametreGlobalService parametreGlobalService;

    // =================================================================
    // 🔹 VÉRIFICATION QUOTA (par MATRICULE)
    // =================================================================

    @Transactional(readOnly = true)
    public boolean quotaDisponibleParMatricule(String matricule, String type) {
        // 1. Trouver l'employé par matricule
        Employe emp = employeService.trouverParMatricule(matricule);

        // 2. Compter les autorisations du même type ce mois-ci
        LocalDate premierJourMois = LocalDate.now().withDayOfMonth(1);
        long count = autorisationRepo.countByEmployeIdAndTypeAndMonth(
                emp.getId(), type, premierJourMois);

        int maxAutorisations = parametreGlobalService.getValeurAsInt("MAX_AUTORISATIONS_MOIS", 2);
        log.debug("Quota vérifié pour matricule {}: {} autorisations (max {})", matricule, count, maxAutorisations);
        return count < maxAutorisations;
    }

    // =================================================================
    // 🔹 CRÉATION DE DEMANDE (par MATRICULE)
    // =================================================================

    @Transactional
    public Autorisation creerDemandeParMatricule(String matricule, AutorisationRequestDto request) {
        // 1. Trouver l'employé par matricule
        Employe emp = employeService.trouverParMatricule(matricule);

        // 2. Calculer durée en heures
        Double duree = calculerDuree(request.getHeureDebut(), request.getHeureFin());

        // 3. Créer l'entité Autorisation
        Autorisation aut = Autorisation.builder()
                .employe(emp)
                .date(request.getDate())
                .heureDebut(request.getHeureDebut())
                .heureFin(request.getHeureFin())
                .type(request.getType())           // PERSONNEL ou SERVICE
                .motif(request.getMotif())         // Description du motif
                .statut("EN_ATTENTE")              // Statut initial
                .dureeHeures(duree)                // Durée calculée
                .estPayee(false)                   // Par défaut non payée
                .build();

        // 4. Calculer automatiquement la durée (méthode métier de l'entity)
        aut.calculerDuree();

        // 5. Logger l'opération (sans utiliser getMatricule() si problème)
        log.info("📝 Nouvelle demande: employeId={}, matricule={}, type={}, date={}, duree={}h",
                emp.getId(), matricule, request.getType(), request.getDate(), duree);

        // 6. Sauvegarder en base
        return autorisationRepo.save(aut);
    }

    // =================================================================
    // 🔹 UTILITAIRE: Calculer durée entre deux LocalTime
    // =================================================================

    private Double calculerDuree(LocalTime debut, LocalTime fin) {
        if (debut != null && fin != null && !fin.isBefore(debut)) {
            long seconds = fin.toSecondOfDay() - debut.toSecondOfDay();
            return (double) seconds / 3600;
        }
        return 0.0;
    }

    // =================================================================
    // 🔹 VALIDER UNE DEMANDE
    // =================================================================

    @Transactional
    public Autorisation validerDemande(Long autorisationId) {
        Autorisation aut = autorisationRepo.findById(autorisationId)
                .orElseThrow(() -> new RuntimeException("Autorisation non trouvée: " + autorisationId));

        aut.setStatut("VALIDEE");
        // Optionnel: aut.mettreAJourSolde(); si la méthode existe dans l'entity
        autorisationRepo.save(aut);

        log.info("✅ Autorisation {} validée", autorisationId);
        return aut;
    }

    // =================================================================
    // 🔹 REJETER UNE DEMANDE
    // =================================================================

    @Transactional
    public Autorisation rejeterDemande(Long autorisationId, String motifRejet) {
        Autorisation aut = autorisationRepo.findById(autorisationId)
                .orElseThrow(() -> new RuntimeException("Autorisation non trouvée: " + autorisationId));

        aut.setStatut("REJETEE");
        autorisationRepo.save(aut);

        log.info("❌ Autorisation {} rejetée: {}", autorisationId, motifRejet);
        return aut;
    }

    // =================================================================
    // 🔹 CONSULTATION: Trouver par ID
    // =================================================================

    @Transactional(readOnly = true)
    public Autorisation trouverParId(Long id) {
        return autorisationRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Autorisation non trouvée: " + id));
    }

    // =================================================================
    // 🔹 HISTORIQUE PAR MATRICULE (avec filtre statut optionnel)
    // =================================================================

    @Transactional(readOnly = true)
    public List<Autorisation> getHistoriqueParMatricule(String matricule, String statut) {
        // 1. Trouver l'employé par matricule
        Employe emp = employeService.trouverParMatricule(matricule);

        // 2. Appliquer le filtre statut si fourni
        if (statut == null || statut.isEmpty() || "TOUS".equalsIgnoreCase(statut)) {
            return autorisationRepo.findByEmployeOrderByDateDesc(emp);
        }
        return autorisationRepo.findByEmployeAndStatutOrderByDateDesc(emp, statut.toUpperCase());
    }

    // =================================================================
    // 🔹 HISTORIQUE PAR PÉRIODE (par matricule)
    // =================================================================

    @Transactional(readOnly = true)
    public List<Autorisation> getHistoriqueParPeriode(String matricule, LocalDate debut, LocalDate fin) {
        Employe emp = employeService.trouverParMatricule(matricule);
        return autorisationRepo.findByEmployeAndDateBetween(emp, debut, fin);
    }

    // =================================================================
    // 🔹 EXPORT: Préparer les données pour PDF/Excel (DTO)
    // =================================================================

    @Transactional(readOnly = true)
    public AutorisationDto exporterAutorisation(Long id) {
        Autorisation aut = trouverParId(id);
        Employe emp = aut.getEmploye();

        return AutorisationDto.builder()
                .id(aut.getId())
                .matricule(emp.getMatricule())
                .nomComplet(emp.getNom() + " " + emp.getPrenom())  // Alternative à getFullName()
                .departement(emp.getDepartement() != null ? emp.getDepartement().getNom() : "N/A")
                .agence(emp.getAgence() != null ? emp.getAgence().getNom() : "N/A")
                .date(aut.getDate())
                .heureDebut(aut.getHeureDebut())
                .heureFin(aut.getHeureFin())
                .dureeHeures(aut.getDureeHeures())
                .statut(aut.getStatut())
                .type(aut.getType())
                .motif(aut.getMotif())
                .estPayee(aut.getEstPayee())
                .processInstanceId(aut.getProcessInstanceId())
                .build();
    }

    // =================================================================
    // 🔹 STATISTIQUES: Compteur par statut et mois (pour dashboard RH)
    // =================================================================

    @Transactional(readOnly = true)
    public long countByStatutAndMois(String statut, LocalDate mois) {
        LocalDate debut = mois.withDayOfMonth(1);
        LocalDate fin = mois.with(TemporalAdjusters.lastDayOfMonth());
        return autorisationRepo.countByStatutAndDateBetween(statut, debut, fin);
    }

    // =================================================================
    // 🔹 VÉRIFIER SI UNE DEMANDE EXISTE DÉJÀ (pour éviter les doublons)
    // =================================================================

    @Transactional(readOnly = true)
    public boolean demandeExisteDeja(String matricule, LocalDate date, String type) {
        Employe emp = employeService.trouverParMatricule(matricule);
        return autorisationRepo.existsByEmployeAndDateAndType(emp, date, type);
    }
    // =================================================================
// 🔹 CHATBOT : Nombre d'autorisations utilisées ce mois
// =================================================================

    @Transactional(readOnly = true)
    public int getNombreAutorisationsMois(Long employeId) {
        LocalDate premierJourMois = LocalDate.now().withDayOfMonth(1);
        // On utilise ton repository existant countByEmployeIdAndTypeAndMonth
        // en comptant tous types confondus (PERSONNEL + SERVICE)
        long countPersonnel = autorisationRepo.countByEmployeIdAndTypeAndMonth(
                employeId, "PERSONNEL", premierJourMois);
        long countService = autorisationRepo.countByEmployeIdAndTypeAndMonth(
                employeId, "SERVICE", premierJourMois);
        return (int)(countPersonnel + countService);
    }
}