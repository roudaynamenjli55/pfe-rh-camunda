package org.example.rhcamunda.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.rhcamunda.entity.Demande;
import org.example.rhcamunda.repository.DemandeRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DemandeService {

    private final DemandeRepository demandeRepository;

    public List<Demande> getDemandesByEmployeMatricule(String matricule) {
        // ✅ Utilisation de la méthode existante dans le Repository
        return demandeRepository.findByEmployeMatricule(matricule);
    }

    public List<Demande> getDemandesByStatut(String statut) {
        // Cette méthode n'existe pas encore dans le Repository, donc on retourne une liste vide ou on l'implémente
        // Pour l'instant, retournons une liste vide pour éviter l'erreur de compilation
        return List.of();
    }

    public List<Demande> getDemandesByPeriode(LocalDate debut, LocalDate fin) {
        // Cette méthode attend un matricule, mais tu passes "" (chaîne vide)
        // Si tu veux toutes les demandes, crée une nouvelle méthode dans le Repository
        return demandeRepository.findByPeriode(debut, fin);
    }

    public long countDemandesByTypeAndMonth(String type, LocalDate date) {
        LocalDate premierJour = date.with(java.time.temporal.TemporalAdjusters.firstDayOfMonth());
        LocalDate dernierJour = date.with(java.time.temporal.TemporalAdjusters.lastDayOfMonth());

        // À implémenter dans le repository si nécessaire
        return 0L;
    }
}