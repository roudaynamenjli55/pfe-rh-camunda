package org.example.rhcamunda.service;

import lombok.RequiredArgsConstructor;
import org.example.rhcamunda.dto.*;
import org.example.rhcamunda.repository.CongeRepository;
import org.example.rhcamunda.repository.EmployeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final EmployeRepository employeRepository;
    private final CongeRepository congeRepository;

    /**
     * 1. STATISTIQUES GÉNÉRALES (Temps Réel - 100% DB)
     */
    public StatsGeneralesDTO getStatsGenerales() {
        long actifs = employeRepository.countByActifTrue();
        long inactifs = employeRepository.countByActifFalse();
        long total = actifs + inactifs;

        // Taux d'absentéisme dynamique : (jours absence / (actifs × 22)) × 100
        LocalDate debutMois = LocalDate.now().withDayOfMonth(1);
        long joursAbsence = congeRepository.sumAbsenceDaysThisMonth(debutMois);
        double joursTheoriques = actifs * 22;
        double tauxAbsentéisme = joursTheoriques > 0 ? (joursAbsence / joursTheoriques) * 100 : 0;

        // Solde moyen via AVG SQL
        Double soldeMoyenObj = employeRepository.avgSoldeCongeActifs();
        double soldeMoyen = soldeMoyenObj != null ? soldeMoyenObj : 0.0;

        // Nouveaux employés ce mois
        long nouveauxMois = employeRepository.countByDateEmbaucheBetweenAndActifTrue(
                debutMois, LocalDate.now());

        return StatsGeneralesDTO.builder()
                .totalEmployes(total)
                .employesActifs(actifs)
                .employesInactifs(inactifs)
                .tauxAbsentéisme(Math.round(tauxAbsentéisme * 100.0) / 100.0)
                .soldeCongeMoyen(Math.round(soldeMoyen * 100.0) / 100.0)
                .nouveauxEmployesMois(nouveauxMois)
                .build();
    }

    /**
     * 2. STATISTIQUES PAR DÉPARTEMENT (Jointure SQL dynamique)
     */
    public List<StatsDepartementDTO> getStatsParDepartement() {
        List<Object[]> raw = employeRepository.countByDepartementNom();
        long total = raw.stream().mapToLong(r -> (Long) r[1]).sum();

        return raw.stream().map(r -> {
                    String nomDept = (String) r[0];
                    long count = (Long) r[1];
                    double pourcentage = total > 0 ? Math.round((count * 100.0 / total) * 100.0) / 100.0 : 0;

                    return StatsDepartementDTO.builder()
                            .departementNom(nomDept)
                            .nombreEmployes(count)
                            .pourcentage(pourcentage)
                            .build();
                })
                .sorted(Comparator.comparing(StatsDepartementDTO::getNombreEmployes).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 3. STATISTIQUES DES CONGÉS (Agrégations SQL)
     */
    public StatsCongesDTO getStatsConges() {
        long approuves = congeRepository.countByStatut("APPROUVE");
        long enAttente = congeRepository.countByStatut("EN_ATTENTE");
        long refuses = congeRepository.countByStatut("REFUSE");

        LocalDate debutAnnee = LocalDate.now().withDayOfYear(1);
        long totalJoursAnnee = congeRepository.sumApprovedDaysThisYear(debutAnnee);

        long actifs = employeRepository.countByActifTrue();
        double joursMoyens = actifs > 0 ? (double) totalJoursAnnee / actifs : 0;

        return StatsCongesDTO.builder()
                .totalCongesPris((int) totalJoursAnnee)
                .joursMoyensParEmploye(Math.round(joursMoyens * 100.0) / 100.0)
                .congesEnAttente((int) enAttente)
                .congesApprouves((int) approuves)
                .congesRefuses((int) refuses)
                .build();
    }

    /**
     * 4. ÉVOLUTION DES EFFECTIFS (Graphique 12 mois glissants)
     */
    public ChartDataDTO getEffectifsParMois() {
        LocalDate aujourdhui = LocalDate.now();
        LocalDate ilYaUnAn = aujourdhui.minusYears(1).plusMonths(1);

        List<Object[]> raw = employeRepository.countHiresByMonthNative(ilYaUnAn);

        // Pré-remplir les 12 mois avec 0
        Map<String, Integer> monthlyMap = new LinkedHashMap<>();
        for (int i = 0; i < 12; i++) {
            LocalDate m = ilYaUnAn.plusMonths(i);
            String key = m.getYear() + "-" + String.format("%02d", m.getMonthValue());
            monthlyMap.put(key, 0);
        }

        // Remplacer par les vraies données DB
        raw.forEach(row -> {
            String moisDB = (String) row[0];
            Long count = ((Number) row[1]).longValue();
            if (monthlyMap.containsKey(moisDB)) {
                monthlyMap.put(moisDB, count.intValue());
            }
        });

        // Formatter les labels (ex: "Jan 2026")
        List<String> labels = monthlyMap.keySet().stream()
                .map(k -> {
                    String[] parts = k.split("-");
                    int year = Integer.parseInt(parts[0]);
                    int month = Integer.parseInt(parts[1]);
                    return LocalDate.of(year, month, 1)
                            .getMonth().getDisplayName(java.time.format.TextStyle.SHORT, new Locale("fr"))
                            + " " + year;
                }).collect(Collectors.toList());

        return ChartDataDTO.builder()
                .labels(labels)
                .data(new ArrayList<>(monthlyMap.values()))
                .build();
    }
}