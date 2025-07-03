package com.example.homecare_adm_app.api.controller;


import com.example.homecare_adm_app.api.dto.AgendamentoDTO;
import com.example.homecare_adm_app.api.dto.MessageResponseDTO;
import com.example.homecare_adm_app.api.service.AgendamentoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/agendamentos")
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoService agendamentoService;

    @GetMapping
    public ResponseEntity<List<AgendamentoDTO>> getAgendamentosByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data
    ) {
        return ResponseEntity.ok(agendamentoService.getAgendamentosByDate(data));
    }

    @GetMapping("/hoje")
    public ResponseEntity<List<AgendamentoDTO>> getAgendamentosHoje() {
        return ResponseEntity.ok(agendamentoService.getAgendamentosHoje());
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<AgendamentoDTO>> getAgendamentosByPaciente(
            @PathVariable Long pacienteId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        return ResponseEntity.ok(agendamentoService.getAgendamentosByPaciente(pacienteId, dataInicio, dataFim));
    }

    @GetMapping("/profissional/{profissionalId}")
    public ResponseEntity<List<AgendamentoDTO>> getAgendamentosByProfissional(
            @PathVariable Long profissionalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        return ResponseEntity.ok(agendamentoService.getAgendamentosByProfissional(profissionalId, dataInicio, dataFim));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AgendamentoDTO> getAgendamentoById(@PathVariable Long id) {
        return ResponseEntity.ok(agendamentoService.getAgendamentoById(id));
    }

    @PostMapping
    public ResponseEntity<AgendamentoDTO> createAgendamento(@Valid @RequestBody AgendamentoDTO agendamentoDTO) {
        AgendamentoDTO created = agendamentoService.createAgendamento(agendamentoDTO);
        return ResponseEntity.created(URI.create("/agendamentos/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AgendamentoDTO> updateAgendamento(
            @PathVariable Long id,
            @Valid @RequestBody AgendamentoDTO agendamentoDTO
    ) {
        return ResponseEntity.ok(agendamentoService.updateAgendamento(id, agendamentoDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDTO> deleteAgendamento(@PathVariable Long id) {
        agendamentoService.deleteAgendamento(id);
        return ResponseEntity.ok(new MessageResponseDTO("Agendamento excluído com sucesso"));
    }
}
