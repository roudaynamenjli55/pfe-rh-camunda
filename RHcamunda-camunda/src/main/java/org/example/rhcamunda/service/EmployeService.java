package org.example.rhcamunda.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rhcamunda.entity.Employe;
import org.example.rhcamunda.repository.EmployeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmployeService {

    private final EmployeRepository employeRepository;

    // =================================================================
    // 🔹 CRUD : CREATE
    // =================================================================

    @Transactional
    public Employe creerEmploye(Employe employe) {
        // Vérification unicité matricule
        if (employe.getMatricule() != null && employeRepository.existsByMatricule(employe.getMatricule())) {
            throw new RuntimeException("Matricule déjà utilisé : " + employe.getMatricule());
        }

        // Initialisation contexte bancaire
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
        log.info("✅ Employé créé : {} - {}", saved.getMatricule(), saved.getNomComplet());
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
        return employeRepository.findByDepartementId(departementId);
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
        emp.setActif(false);
        emp.setDateArretChivage(java.time.LocalDateTime.now());
        employeRepository.save(emp);
        log.info("🗑️ Employé archivé : {}", emp.getMatricule());
    }

    // =================================================================
    // 🔹 WORKFLOW HELPERS
    // =================================================================

    @Transactional(readOnly = true)
    public int getSoldeConges(String matricule) {
        Employe emp = trouverParMatricule(matricule);
        return emp.getSoldeConge() != null ? emp.getSoldeConge().intValue() : 0;
    }

    @Transactional(readOnly = true)
    public int getSoldeAutorisations(String matricule) {
        Employe emp = trouverParMatricule(matricule);
        return emp.getSoldeAutorisation() != null ? emp.getSoldeAutorisation() : 0;
    }

    @Transactional(readOnly = true)
    public Employe trouverManagerParEmployeMatricule(String matricule) {
        Employe emp = trouverParMatricule(matricule);
        if (emp.getSuperieur() == null) {
            throw new RuntimeException("Aucun manager défini pour l'employé : " + matricule);
        }
        return emp.getSuperieur();
    }

    // =================================================================
    // 🔹 STATS & COMPTAGES
    // =================================================================

    @Transactional(readOnly = true)
    public long countEmployesActifs() {
        return employeRepository.countByActifTrue();
    }

    @Transactional(readOnly = true)
    public long countByDepartement() {
        return employeRepository.count();
    }

    @Transactional(readOnly = true)
    public long countNouveauxCeMois() {
        LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
        return employeRepository.countByDateEmbaucheBetween(debutMois, LocalDate.now().plusDays(1));
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
}