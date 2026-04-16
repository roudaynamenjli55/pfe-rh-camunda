package org.example.rhcamunda.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rhcamunda.dto.EmployeDTO;
import org.example.rhcamunda.entity.Employe;
import org.example.rhcamunda.service.EmployeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/employes")
@RequiredArgsConstructor
@Slf4j
public class EmployeController {

    private final EmployeService employeService;

    // =================================================================
    // 🔹 CRUD : CREATE
    // =================================================================

    @PostMapping
    @PreAuthorize("hasRole('RH') or hasRole('ADMIN')")
    public ResponseEntity<EmployeDTO> creerEmploye(@Valid @RequestBody EmployeDTO dto) {

        log.info("📝 Création employé demandée par: {}", getCurrentUser());

        // 1. Conversion DTO → Entity
        Employe employe = dto.toEntity();

        // 2. 🔹 FORCER LE MATRICULE À NULL pour qu'il soit généré automatiquement
        employe.setMatricule(null);

        // 3. Appel au service
        Employe cree = employeService.creerEmploye(employe);

        // 4. Conversion Entity → DTO pour la réponse
        EmployeDTO response = EmployeDTO.fromEntity(cree);

        log.info("✅ Employé créé avec succès : {} (Matricule: {})",
                response.getNomComplet(), response.getMatricule());

        return ResponseEntity.ok(response);
    }

    // =================================================================
    // 🔹 CRUD : READ (Liste & Détail)
    // =================================================================

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLOYE', 'MANAGER', 'RH', 'ADMIN')")
    public ResponseEntity<List<EmployeDTO>> listerEmployes(
            @RequestParam(required = false) Long departementId,
            @RequestParam(required = false) String keyword) {

        List<Employe> employes;

        if (departementId != null) {
            employes = employeService.listerEmployesParDepartement(departementId);
        } else if (keyword != null && !keyword.isEmpty()) {
            employes = employeService.rechercherEmployes(keyword);
        } else {
            employes = employeService.listerEmployes();
        }

        List<EmployeDTO> dtos = employes.stream()
                .map(EmployeDTO::fromEntity)
                .toList();

        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('RH', 'ADMIN')")
    public ResponseEntity<Page<EmployeDTO>> listerEmployesPagines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "nom") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Employe> pageEmployes = employeService.listerEmployesPagines(pageable);

        Page<EmployeDTO> dtoPage = pageEmployes.map(EmployeDTO::fromEntity);

        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLOYE', 'MANAGER', 'RH', 'ADMIN')")
    public ResponseEntity<EmployeDTO> getEmployeById(@PathVariable Long id) {

        Employe employe = employeService.trouverParId(id);
        EmployeDTO dto = EmployeDTO.fromEntity(employe);

        // 🔹 Vérification d'accès : un employé ne voit que son propre profil
        if (!aDroitDeVoir(id)) {
            return ResponseEntity.status(403).build();
        }

        return ResponseEntity.ok(dto);
    }

    @GetMapping("/matricule/{matricule}")
    @PreAuthorize("hasAnyRole('MANAGER', 'RH', 'ADMIN')")
    public ResponseEntity<EmployeDTO> getEmployeByMatricule(@PathVariable String matricule) {

        Employe employe = employeService.trouverParMatricule(matricule);
        EmployeDTO dto = EmployeDTO.fromEntity(employe);

        return ResponseEntity.ok(dto);
    }

    // =================================================================
    // 🔹 CRUD : UPDATE
    // =================================================================

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RH') or hasRole('ADMIN')")
    public ResponseEntity<EmployeDTO> mettreAJourEmploye(
            @PathVariable Long id,
            @Valid @RequestBody EmployeDTO dto) {

        log.info("✏️ Mise à jour employé ID: {} par: {}", id, getCurrentUser());

        // 1. Récupérer l'employé existant
        Employe existing = employeService.trouverParId(id);

        // 2. Mise à jour des champs modifiables (NE JAMAIS modifier matricule/id)
        existing.setNom(dto.getNom());
        existing.setPrenom(dto.getPrenom());
        existing.setEmail(dto.getEmail());
        existing.setTelephone(dto.getTelephone());
        existing.setAdresse(dto.getAdresse());
        existing.setDateNaissance(dto.getDateNaissance());

        // 3. Sauvegarde
        Employe updated = employeService.mettreAJourEmploye(id, existing);
        EmployeDTO response = EmployeDTO.fromEntity(updated);

        log.info("✅ Employé mis à jour : {}", response.getMatricule());

        return ResponseEntity.ok(response);
    }

    // =================================================================
    // 🔹 CRUD : DELETE (Soft Delete / Archive)
    // =================================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RH') or hasRole('ADMIN')")
    public ResponseEntity<Void> archiverEmploye(@PathVariable Long id) {

        log.info("🗑️ Archivage employé ID: {} par: {}", id, getCurrentUser());

        employeService.archiverEmploye(id);

        log.info("✅ Employé archivé : ID={}", id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/reactiver")
    @PreAuthorize("hasRole('RH') or hasRole('ADMIN')")
    public ResponseEntity<EmployeDTO> reactiverEmploye(@PathVariable Long id) {

        Employe employe = employeService.trouverParId(id);
        employe.setActif(true);
        employe.setDateArretChivage(null);

        Employe updated = employeService.mettreAJourEmploye(id, employe);

        return ResponseEntity.ok(EmployeDTO.fromEntity(updated));
    }

    // =================================================================
    // 🔹 WORKFLOW HELPERS (Congés & Autorisations)
    // =================================================================

    @GetMapping("/{matricule}/solde-conges")
    @PreAuthorize("hasAnyRole('EMPLOYE', 'MANAGER', 'RH')")
    public ResponseEntity<Double> getSoldeConges(@PathVariable String matricule) {

        // 🔹 Un employé ne voit que son propre solde
        if (!estMemeEmploye(matricule)) {
            return ResponseEntity.status(403).build();
        }

        // ✅ CORRECTION ICI : Retourner Double au lieu de int
        Double solde = employeService.getSoldeConges(matricule);
        return ResponseEntity.ok(solde);
    }

    @GetMapping("/{matricule}/solde-autorisations")
    @PreAuthorize("hasAnyRole('EMPLOYE', 'MANAGER', 'RH')")
    public ResponseEntity<Integer> getSoldeAutorisations(@PathVariable String matricule) {

        if (!estMemeEmploye(matricule)) {
            return ResponseEntity.status(403).build();
        }

        Integer solde = employeService.getSoldeAutorisations(matricule);
        return ResponseEntity.ok(solde);
    }

    @GetMapping("/{matricule}/manager")
    @PreAuthorize("hasAnyRole('EMPLOYE', 'MANAGER', 'RH')")
    public ResponseEntity<EmployeDTO> getManager(@PathVariable String matricule) {

        if (!estMemeEmploye(matricule)) {
            return ResponseEntity.status(403).build();
        }

        Employe manager = employeService.trouverManagerParEmployeMatricule(matricule);
        return ResponseEntity.ok(EmployeDTO.fromEntity(manager));
    }

    // =================================================================
    // 🔹 STATS & DASHBOARD (RH uniquement)
    // =================================================================

    @GetMapping("/stats/effectif")
    @PreAuthorize("hasRole('RH') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStatsEffectif() {

        long total = employeService.countEmployesActifs();

        // ✅ CORRECTION ICI : Utiliser une Map au lieu de long
        Map<Long, Long> parDepartement = employeService.countByDepartement();

        long nouveauxCeMois = employeService.countNouveauxCeMois();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalActifs", total);
        stats.put("parDepartement", parDepartement);
        stats.put("nouveauxCeMois", nouveauxCeMois);
        stats.put("dateGeneration", java.time.LocalDateTime.now());

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/stats/soldes-moyens")
    @PreAuthorize("hasRole('RH') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Double>> getSoldesMoyens() {

        double soldeCongeMoyen = employeService.getSoldeCongeMoyen();
        double soldeAutorisationMoyen = employeService.getSoldeAutorisationMoyen();

        return ResponseEntity.ok(Map.of(
                "soldeCongeMoyen", soldeCongeMoyen,
                "soldeAutorisationMoyen", soldeAutorisationMoyen
        ));
    }

    // =================================================================
    // 🔹 MÉTHODES UTILITAIRES (Sécurité & Logs)
    // =================================================================

    private String getCurrentUser() {
        return "system"; // Temporaire pour dev
    }

    private boolean aDroitDeVoir(Long employeId) {
        return true; // Temporaire pour dev
    }

    private boolean estMemeEmploye(String matricule) {
        return true; // Temporaire pour dev
    }
}