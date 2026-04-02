package org.example.rhcamunda.service;


import org.example.rhcamunda.repository.UtilisateurRepository;

import org.example.rhcamunda.dto.UtilisateurDTO;
import org.example.rhcamunda.entity.Role;
import org.example.rhcamunda.entity.Utilisateur;
import org.example.rhcamunda.repository.RoleRepository; // Vérifie enou mawjouda hethi kman

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UtilisateurService {

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Autowired
    private RoleRepository roleRepository;

    // Récupérer tous les utilisateurs
    public List<UtilisateurDTO> getAllUtilisateurs() {
        return utilisateurRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Créer un nouvel utilisateur
    public UtilisateurDTO createUtilisateur(UtilisateurDTO dto) {
        // Vérifier si l'email existe déjà
        if (utilisateurRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Cet email existe déjà !");
        }

        // Récupérer le rôle depuis la base de données
        Role role = roleRepository.findById(dto.getRoleId())
                .orElseThrow(() -> new RuntimeException("Rôle non trouvé"));

        // Convertir DTO -> Entity
        Utilisateur entity = toEntity(dto, role);

        // Sauvegarder
        Utilisateur savedEntity = utilisateurRepository.save(entity);

        return toDto(savedEntity);
    }

    // Supprimer un utilisateur
    public void deleteUtilisateur(Long id) {
        utilisateurRepository.deleteById(id);
    }

    // Convertir Entity -> DTO
    private UtilisateurDTO toDto(Utilisateur entity) {
        UtilisateurDTO dto = new UtilisateurDTO();
        dto.setId(entity.getId());
        dto.setLogin(entity.getLogin());
        dto.setNom(entity.getNom());
        dto.setPrenom(entity.getPrenom());
        dto.setEmail(entity.getEmail());
        dto.setEnabled(entity.getEnabled());
        dto.setDateCreation(entity.getDateCreation());

        if (entity.getRole() != null) {
            dto.setRoleId(entity.getRole().getId());
            dto.setRoleCode(entity.getRole().getCode());
            dto.setRoleLibelle(entity.getRole().getLibelle());
        }

        // IMPORTANT: Ne jamais renvoyer le mot de passe
        return dto;
    }

    // Convertir DTO -> Entity
    private Utilisateur toEntity(UtilisateurDTO dto, Role role) {
        Utilisateur entity = new Utilisateur();
        entity.setLogin(dto.getLogin());
        entity.setNom(dto.getNom());
        entity.setPrenom(dto.getPrenom());
        entity.setEmail(dto.getEmail());
        entity.setMotDePasse(dto.getMotDePasse()); // À hacher en prod!
        entity.setRole(role);
        entity.setEnabled(true);
        return entity;
    }
}