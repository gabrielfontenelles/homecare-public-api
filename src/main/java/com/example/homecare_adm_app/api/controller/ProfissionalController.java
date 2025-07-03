package com.example.homecare_adm_app.api.controller;

import com.example.homecare_adm_app.api.dto.MessageResponseDTO;
import com.example.homecare_adm_app.api.dto.ProfissionalDTO;
import com.example.homecare_adm_app.api.service.ProfissionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/profissionais")
@RequiredArgsConstructor
public class ProfissionalController {

    private final ProfissionalService profissionalService;

    @GetMapping
    public ResponseEntity<Page<ProfissionalDTO>> getAllProfissionais(
            @RequestParam(required = false) String nome,
            @RequestParam(required = false) String especialidade,
            @RequestParam(required = false) String status,
            Pageable pageable
    ) {
        return ResponseEntity.ok(profissionalService.getAllProfissionais(nome, especialidade, status, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProfissionalDTO> getProfissionalById(@PathVariable Long id) {
        return ResponseEntity.ok(profissionalService.getProfissionalById(id));
    }

    @PostMapping
    public ResponseEntity<ProfissionalDTO> createProfissional(@Valid @RequestBody ProfissionalDTO profissionalDTO) {
        ProfissionalDTO created = profissionalService.createProfissional(profissionalDTO);
        return ResponseEntity.created(URI.create("/profissionais/" + created.getId())).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProfissionalDTO> updateProfissional(
            @PathVariable Long id,
            @Valid @RequestBody ProfissionalDTO profissionalDTO
    ) {
        return ResponseEntity.ok(profissionalService.updateProfissional(id, profissionalDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDTO> deleteProfissional(@PathVariable Long id) {
        profissionalService.deleteProfissional(id);
        return ResponseEntity.ok(new MessageResponseDTO("Profissional excluído com sucesso"));
    }

    @PutMapping("/{id}/reativar")
    public ResponseEntity<MessageResponseDTO> reativarProfissional(@PathVariable Long id) {
        profissionalService.reativarProfissional(id);
        return ResponseEntity.ok(new MessageResponseDTO("Profissional reativado com sucesso"));
    }
} 