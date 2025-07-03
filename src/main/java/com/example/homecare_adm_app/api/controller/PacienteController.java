package com.example.homecare_adm_app.api.controller;

import com.example.homecare_adm_app.api.dto.MessageResponseDTO;
import com.example.homecare_adm_app.api.dto.PacienteDTO;
import com.example.homecare_adm_app.api.service.PacienteService;
import com.example.homecare_adm_app.api.service.DashboardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final PacienteService pacienteService;
    private final DashboardService dashboardService;

    @GetMapping("/recent")
    public ResponseEntity<?> getRecentPatients(@RequestParam(defaultValue = "5") int limit) {
        List<PacienteDTO> recentPacientes = pacienteService.getRecentPacientes(limit);

        List<Map<String, Object>> patients = recentPacientes
                .stream()
                .map(paciente -> {
                    Map<String, Object> patient = new HashMap<>();
                    patient.put("id", paciente.getId());
                    patient.put("nome", paciente.getNome());
                    patient.put("specialty", "Geral"); // Implementar especialidade
                    patient.put("status", paciente.getStatus());
                    patient.put("frequency", "Semanal"); // Implementar frequência
                    return patient;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(patients);
    }

    @GetMapping
    public ResponseEntity<Page<PacienteDTO>> getAllPacientes(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(pacienteService.getAllPacientes(nome, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PacienteDTO> getPacienteById(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.getPacienteById(id));
    }

    @PostMapping
    public ResponseEntity<PacienteDTO> createPaciente(@Valid @RequestBody PacienteDTO pacienteDTO) {
        PacienteDTO created = pacienteService.createPaciente(pacienteDTO);
        return ResponseEntity.created(URI.create("/api/patients/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PacienteDTO> updatePaciente(
            @PathVariable Long id,
            @Valid @RequestBody PacienteDTO pacienteDTO
    ) {
        return ResponseEntity.ok(pacienteService.updatePaciente(id, pacienteDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDTO> deletePaciente(@PathVariable Long id) {
        pacienteService.deletePaciente(id);
        return ResponseEntity.ok(new MessageResponseDTO("Paciente excluído com sucesso"));
    }
}