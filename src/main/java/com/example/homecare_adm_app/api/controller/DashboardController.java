package com.example.homecare_adm_app.api.controller;

import com.example.homecare_adm_app.api.dto.DashboardDTO;
import com.example.homecare_adm_app.api.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        DashboardDTO data = dashboardService.getDashboardData();

        Map<String, Object> response = new HashMap<>();

        
        Map<String, Number> activePatients = new HashMap<>();
        activePatients.put("count", data.getPacientesAtivos());
        activePatients.put("variation", 0); //  Implementar cálculo de variação
        response.put("activePatients", activePatients);

        
        Map<String, Number> todayVisits = new HashMap<>();
        todayVisits.put("total", data.getVisitasAgendadasHoje());
        todayVisits.put("completed", 0); //  Implementar contagem de visitas completadas
        todayVisits.put("pending", data.getVisitasAgendadasHoje());
        response.put("todayVisits", todayVisits);

      
        Map<String, Number> availableProfessionals = new HashMap<>();
        availableProfessionals.put("available", data.getProfissionais().getDisponiveis());
        availableProfessionals.put("total", data.getProfissionais().getCadastrados());
        availableProfessionals.put("variation", 0); 
        response.put("availableProfessionals", availableProfessionals);

        // Monthly Revenue (mock por enquanto)
        Map<String, Number> monthlyRevenue = new HashMap<>();
        monthlyRevenue.put("amount", 0);
        monthlyRevenue.put("variation", 0);
        response.put("monthlyRevenue", monthlyRevenue);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/visits")
    public ResponseEntity<?> getVisits(@RequestParam(defaultValue = "30") int days) {
        DashboardDTO data = dashboardService.getDashboardData();

        List<Map<String, Object>> visits = data.getVisitasUltimos30Dias()
                .stream()
                .map(visita -> {
                    Map<String, Object> visit = new HashMap<>();
                    visit.put("day", Integer.parseInt(visita.getDia()));
                    visit.put("visits", visita.getVisitas());
                    return visit;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(visits);
    }

    
    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboardData() {
        DashboardDTO data = dashboardService.getDashboardData();
        Map<String, Object> response = new HashMap<>();
        response.put("totalPacientes", data.getPacientesAtivos());
        response.put("totalVisitas", data.getTotalVisitas());
        response.put("visitasHoje", data.getVisitasAgendadasHoje());
        response.put("visitasUltimos30Dias", data.getVisitasUltimos30Dias());
        return ResponseEntity.ok(response);
    }
}