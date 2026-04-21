package org.example.rhcamunda.controller;

import lombok.RequiredArgsConstructor;
import org.example.rhcamunda.dto.*;
import org.example.rhcamunda.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats-generales")
    public ResponseEntity<StatsGeneralesDTO> getStatsGenerales() {
        return ResponseEntity.ok().body(dashboardService.getStatsGenerales());
    }

    @GetMapping("/stats-departements")
    public ResponseEntity<List<StatsDepartementDTO>> getStatsParDepartement() {
        // Force le type avec une variable explicite
        List<StatsDepartementDTO> result = dashboardService.getStatsParDepartement();
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/stats-conges")
    public ResponseEntity<StatsCongesDTO> getStatsConges() {
        return ResponseEntity.ok().body(dashboardService.getStatsConges());
    }

    @GetMapping("/effectifs-par-mois")
    public ResponseEntity<ChartDataDTO> getEffectifsParMois() {
        return ResponseEntity.ok().body(dashboardService.getEffectifsParMois());
    }

    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getAll() {
        Map<String, Object> response = new HashMap<>();
        response.put("generales", dashboardService.getStatsGenerales());
        response.put("departements", dashboardService.getStatsParDepartement());
        response.put("conges", dashboardService.getStatsConges());
        response.put("effectifs", dashboardService.getEffectifsParMois());
        return ResponseEntity.ok().body(response);
    }
}