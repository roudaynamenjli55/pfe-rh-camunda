package org.example.rhcamunda.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rhcamunda.entity.Employe;
import org.example.rhcamunda.repository.EmployeRepository;
import org.example.rhcamunda.util.MatriculeGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeService {

    private final EmployeRepository employeRepository;
    private final MatriculeGenerator matriculeGenerator;

    // =================================================================
    // 🔹 CRUD : CREATE
    // =================================================================

    @Transactional
    public Employe creerEmploye(Employe employe) {

        // ✅ Génération automatique du matricule si non fourni
        if (employe.getMatricule() == null || employe.getMatricule().trim().isEmpty()) {
            String matriculeGenere = matriculeGenerator.genererMatricule();
            employe.setMatricule(matriculeGenere);
            log.debug("Matricule auto-généré : {}", matriculeGenere);
        } else {
            // Validation du format
            if (!matriculeGenerator.estFormatValide(employe.getMatricule())) {
                throw new IllegalArgumentException("Format de matricule invalide : " + employe.getMatricule());
            }
            // Vérification d'unicité
            if (employeRepository.existsByMatricule(employe.getMatricule())) {
                throw new RuntimeException("Matricule déjà utilisé : " + employe.getMatricule());
            }
        }

        // Initialisation des valeurs par défaut
        if (employe.getSoldeConge() == null) {
            employe.setSoldeConge(30.0);
        }
        if (employe.getSoldeAutorisation() == null) {
            employe.setSoldeAutorisation(12);
        }
        if (employe.getDateEmbauche() == null) {
            employe.setDateEmbauche(LocalDate.now());
        }
        if (employe.getActif() == null) {
            employe.setActif(true);
        }

        Employe saved = employeRepository.save(employe);
        log.info("✅ Employé créé : {} - Matricule: {}", saved.getNomComplet(), saved.getMatricule());

        return saved;
    }

    // =================================================================
    // 🔹 CRUD : READ
    // =================================================================

    @Transactional(readOnly = true)
    public List<Employe> listerEmployes() {
        return employeRepository.findByActifTrue();
    }

    @Transactional(readOnly = true)
    public List<Employe> listerEmployesParDepartement(Long departementId) {
        return employeRepository.findByDepartementIdAndActifTrue(departementId);
    }

    @Transactional(readOnly = true)
    public List<Employe> listerEmployesParAgence(Long agenceId) {
        return employeRepository.findByAgenceIdAndActifTrue(agenceId);
    }

    @Transactional(readOnly = true)
    public List<Employe> rechercherEmployes(String keyword) {
        return employeRepository.searchByName(keyword);
    }

    @Transactional(readOnly = true)
    public Page<Employe> listerEmployesPagines(Pageable pageable) {
        return employeRepository.findByActifTrue(pageable);
    }

    @Transactional(readOnly = true)
    public Employe trouverParId(Long id) {
        return employeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé avec ID: " + id));
    }

    @Transactional(readOnly = true)
    public Employe trouverParMatricule(String matricule) {
        return employeRepository.findByMatricule(matricule)
                .orElseThrow(() -> new RuntimeException("Employé non trouvé avec matricule: " + matricule));
    }

    // =================================================================
    // 🔹 CRUD : UPDATE
    // =================================================================

    @Transactional
    public Employe mettreAJourEmploye(Long id, Employe details) {
        Employe existing = trouverParId(id);

        existing.setNom(details.getNom());
        existing.setPrenom(details.getPrenom());
        existing.setEmail(details.getEmail());
        existing.setTelephone(details.getTelephone());
        existing.setAdresse(details.getAdresse());
        existing.setDateNaissance(details.getDateNaissance());

        existing.setDepartement(details.getDepartement());
        existing.setPoste(details.getPoste());
        existing.setSuperieur(details.getSuperieur());
        existing.setAgence(details.getAgence());

        log.info("✏️ Employé mis à jour : {}", existing.getMatricule());
        return employeRepository.save(existing);
    }

    // =================================================================
    // 🔹 CRUD : DELETE (Soft Delete)
    // =================================================================

    @Transactional
    public void archiverEmploye(Long id) {
        Employe emp = trouverParId(id);

        if (!emp.getActif()) {
            log.warn("Employé déjà archivé : {}", emp.getMatricule());
            return;
        }

        emp.setActif(false);
        emp.setDateArretChivage(java.time.LocalDateTime.now());
        employeRepository.save(emp);

        log.info("🗑️ Employé archivé : {}", emp.getMatricule());
    }

    @Transactional
    public void restaurerEmploye(Long id) {
        Employe emp = trouverParId(id);
        emp.setActif(true);
        emp.setDateArretChivage(null);
        employeRepository.save(emp);
        log.info("♻️ Employé restauré : {}", emp.getMatricule());
    }

    // =================================================================
    // 🔹 WORKFLOW HELPERS
    // =================================================================

    @Transactional(readOnly = true)
    public Double getSoldeConges(String matricule) {
        Employe emp = trouverParMatricule(matricule);
        return emp.getSoldeConge() != null ? emp.getSoldeConge() : 0.0;
    }

    @Transactional(readOnly = true)
    public Integer getSoldeAutorisations(String matricule) {
        Employe emp = trouverParMatricule(matricule);
        return emp.getSoldeAutorisation() != null ? emp.getSoldeAutorisation() : 0;
    }

    @Transactional
    public void debiterSoldeConge(String matricule, double jours) {
        Employe emp = trouverParMatricule(matricule);
        Double soldeActuel = emp.getSoldeConge() != null ? emp.getSoldeConge() : 0.0;

        if (soldeActuel < jours) {
            throw new RuntimeException("Solde de congé insuffisant. Solde: " + soldeActuel + ", Demandé: " + jours);
        }

        emp.setSoldeConge(soldeActuel - jours);
        employeRepository.save(emp);
        log.info("💳 Solde congé débité : {} - {} jours", matricule, jours);
    }

    @Transactional
    public void debiterSoldeAutorisation(String matricule, int quantite) {
        Employe emp = trouverParMatricule(matricule);
        Integer soldeActuel = emp.getSoldeAutorisation() != null ? emp.getSoldeAutorisation() : 0;

        if (soldeActuel < quantite) {
            throw new RuntimeException("Solde d'autorisation insuffisant");
        }

        emp.setSoldeAutorisation(soldeActuel - quantite);
        employeRepository.save(emp);
        log.info("💳 Solde autorisation débité : {} - {}", matricule, quantite);
    }

    @Transactional(readOnly = true)
    public Employe trouverManagerParEmployeMatricule(String matricule) {
        Employe emp = trouverParMatricule(matricule);
        if (emp.getSuperieur() == null || !emp.getSuperieur().getActif()) {
            throw new RuntimeException("Aucun manager défini pour l'employé : " + matricule);
        }
        return emp.getSuperieur();
    }

    // =================================================================
    // 🔹 STATS & COMPTAGES (Dashboard RH)
    // =================================================================

    @Transactional(readOnly = true)
    public long countEmployesActifs() {
        return employeRepository.countByActifTrue();
    }

    @Transactional(readOnly = true)
    public long countEmployesParDepartement(Long departementId) {
        return employeRepository.countByDepartementIdAndActifTrue(departementId);
    }

    @Transactional(readOnly = true)
    public long countEmployesParAgence(Long agenceId) {
        return employeRepository.countByAgenceIdAndActifTrue(agenceId);
    }

    @Transactional(readOnly = true)
    public long countNouveauxCeMois() {
        LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
        return employeRepository.countByDateEmbaucheBetweenAndActifTrue(debutMois, LocalDate.now().plusDays(1));
    }

    @Transactional(readOnly = true)
    public long countNouveauxCetteAnnee() {
        LocalDate debutAnnee = LocalDate.now().withDayOfYear(1);
        return employeRepository.countByDateEmbaucheBetweenAndActifTrue(debutAnnee, LocalDate.now().plusDays(1));
    }

    @Transactional(readOnly = true)
    public double getSoldeCongeMoyen() {
        return employeRepository.findByActifTrue().stream()
                .mapToDouble(e -> e.getSoldeConge() != null ? e.getSoldeConge() : 0)
                .average()
                .orElse(0.0);
    }

    @Transactional(readOnly = true)
    public double getSoldeAutorisationMoyen() {
        return employeRepository.findByActifTrue().stream()
                .mapToInt(e -> e.getSoldeAutorisation() != null ? e.getSoldeAutorisation() : 0)
                .average()
                .orElse(0.0);
    }

    // =================================================================
    // ✅ CORRECTION : Méthode countByDepartement() AJOUTÉE
    // =================================================================

    @Transactional(readOnly = true)
    public Map<Long, Long> countByDepartement() {
        // Retourne une Map : departementId -> nombre d'employés
        return employeRepository.findByActifTrue().stream()
                .collect(Collectors.groupingBy(
                        e -> e.getDepartement() != null ? e.getDepartement().getId() : 0L,
                        Collectors.counting()
                ));
    }

    // =================================================================
    // 🔹 EXPORT
    // =================================================================

    @Transactional(readOnly = true)
    public List<Employe> exporterEmployesPourRapport() {
        return employeRepository.findByActifTrue().stream()
                .map(emp -> {
                    Employe export = new Employe();
                    export.setMatricule(emp.getMatricule());
                    export.setNom(emp.getNom());
                    export.setPrenom(emp.getPrenom());
                    export.setEmail(emp.getEmail());
                    export.setPoste(emp.getPoste());
                    export.setDepartement(emp.getDepartement());
                    export.setDateEmbauche(emp.getDateEmbauche());
                    export.setSoldeConge(emp.getSoldeConge());
                    return export;
                })
                .toList();
    }
}