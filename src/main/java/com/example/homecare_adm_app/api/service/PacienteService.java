package com.example.homecare_adm_app.api.service;

import com.example.homecare_adm_app.api.dto.PacienteDTO;
import com.example.homecare_adm_app.api.exeption.BadRequestException;
import com.example.homecare_adm_app.api.exeption.ResourceNotFoundException;
import com.example.homecare_adm_app.api.mapper.PacienteMapper;
import com.example.homecare_adm_app.api.model.Paciente;
import com.example.homecare_adm_app.api.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PacienteService {

    private final PacienteRepository pacienteRepository;
    private final PacienteMapper pacienteMapper;

    public Page<PacienteDTO> getAllPacientes(String nome, String status, Pageable pageable) {
        Specification<Paciente> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(nome)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nome")),
                        "%" + nome.toLowerCase() + "%"
                ));
            }

            if (StringUtils.hasText(status)) {
                try {
                    Paciente.StatusPaciente statusEnum = Paciente.StatusPaciente.valueOf(status.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException ignored) {
                    // Ignora status inválido
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return pacienteRepository.findAll(spec, pageable).map(pacienteMapper::toDto);
    }

    public PacienteDTO getPacienteById(Long id) {
        Paciente paciente = pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
        return pacienteMapper.toDto(paciente);
    }

    @Transactional
    public PacienteDTO createPaciente(PacienteDTO pacienteDTO) {
        if (pacienteRepository.count() >= 20) {
            throw new BadRequestException("Limite de 20 pacientes atingido para demonstração.");
        }
        if (pacienteRepository.existsByCpf(pacienteDTO.getCpf())) {
            throw new BadRequestException("Já existe um paciente com este CPF.");
        }
        if (pacienteDTO.getTelefoneResponsavel() == null || pacienteDTO.getTelefoneResponsavel().isEmpty()) {
            throw new BadRequestException("É obrigatório informar um contato de emergência.");
        }
        if (pacienteDTO.getEmail() != null && !pacienteDTO.getEmail().isEmpty() && pacienteRepository.existsByEmail(pacienteDTO.getEmail())) {
            throw new BadRequestException("Já existe um paciente com este email.");
        }
        Paciente paciente = pacienteMapper.toEntity(pacienteDTO);
        Paciente savedPaciente = pacienteRepository.save(paciente);
        return pacienteMapper.toDto(savedPaciente);
    }

    @Transactional
    public PacienteDTO updatePaciente(Long id, PacienteDTO pacienteDTO) {
        if (!pacienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Paciente não encontrado");
        }

        Paciente paciente = pacienteMapper.toEntity(pacienteDTO);
        paciente.setId(id);
        Paciente updatedPaciente = pacienteRepository.save(paciente);
        return pacienteMapper.toDto(updatedPaciente);
    }

    @Transactional
    public void deletePaciente(Long id) {
        if (!pacienteRepository.existsById(id)) {
            throw new ResourceNotFoundException("Paciente não encontrado");
        }
        pacienteRepository.deleteById(id);
    }

    public List<PacienteDTO> getRecentPacientes(int limit) {
        List<Paciente> pacientes = pacienteRepository.findTop5ByOrderByCreatedAtDesc();
        return pacientes.stream()
                .map(pacienteMapper::toDto)
                .toList();
    }

}
