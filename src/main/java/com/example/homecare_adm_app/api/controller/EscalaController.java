package com.example.homecare_adm_app.api.controller;


import com.example.homecare_adm_app.api.dto.EscalaDTO;
import com.example.homecare_adm_app.api.dto.MessageResponseDTO;
import com.example.homecare_adm_app.api.service.EscalaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/escalas")
@RequiredArgsConstructor
public class EscalaController {

    private final EscalaService escalaService;

    @GetMapping
    public ResponseEntity<List<EscalaDTO>> getEscalasByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate data
    ) {
        return ResponseEntity.ok(escalaService.getEscalasByDate(data));
    }

    @GetMapping("/profissional/{profissionalId}")
    public ResponseEntity<List<EscalaDTO>> getEscalasByProfissional(
            @PathVariable Long profissionalId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim
    ) {
        return ResponseEntity.ok(escalaService.getEscalasByProfissional(profissionalId, dataInicio, dataFim));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EscalaDTO> getEscalaById(@PathVariable Long id) {
        return ResponseEntity.ok(escalaService.getEscalaById(id));
    }

    @PostMapping
    public ResponseEntity<EscalaDTO> createEscala(@Valid @RequestBody EscalaDTO escalaDTO) {
        EscalaDTO created = escalaService.createEscala(escalaDTO);
        return ResponseEntity.created(URI.create("/escalas/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EscalaDTO> updateEscala(
            @PathVariable Long id,
            @Valid @RequestBody EscalaDTO escalaDTO
    ) {
        return ResponseEntity.ok(escalaService.updateEscala(id, escalaDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDTO> deleteEscala(@PathVariable Long id) {
        escalaService.deleteEscala(id);
        return ResponseEntity.ok(new MessageResponseDTO("Escala excluída com sucesso"));
    }
}