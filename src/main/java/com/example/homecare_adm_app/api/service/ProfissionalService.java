package com.example.homecare_adm_app.api.service;


import com.example.homecare_adm_app.api.dto.ProfissionalDTO;
import com.example.homecare_adm_app.api.exeption.ResourceNotFoundException;
import com.example.homecare_adm_app.api.mapper.ProfissionalMapper;
import com.example.homecare_adm_app.api.model.Profissional;
import com.example.homecare_adm_app.api.model.User;
import com.example.homecare_adm_app.api.repository.ProfissionalRepository;
import com.example.homecare_adm_app.api.repository.UserRepository;
import com.example.homecare_adm_app.api.repository.EscalaRepository;
import com.example.homecare_adm_app.api.model.Escala;
import com.example.homecare_adm_app.api.model.Profissional.StatusProfissional;
import lombok.RequiredArgsConstructor;
import com.example.homecare_adm_app.api.exeption.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProfissionalService {

    private final ProfissionalRepository profissionalRepository;
    private final UserRepository userRepository;
    private final ProfissionalMapper profissionalMapper;
    private final EscalaRepository escalaRepository;

    public Page<ProfissionalDTO> getAllProfissionais(String nome, String especialidade, String status, Pageable pageable) {
        Specification<Profissional> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(nome)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("nome")),
                        "%" + nome.toLowerCase() + "%"
                ));
            }

            if (StringUtils.hasText(especialidade)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("especialidade")),
                        "%" + especialidade.toLowerCase() + "%"
                ));
            }

            if (StringUtils.hasText(status)) {
                try {
                    Profissional.StatusProfissional statusEnum = Profissional.StatusProfissional.valueOf(status.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException ignored) {
                    // Ignora status inválido
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return profissionalRepository.findAll(spec, pageable).map(profissionalMapper::toDto);
    }

    public ProfissionalDTO getProfissionalById(Long id) {
        Profissional profissional = profissionalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));
        return profissionalMapper.toDto(profissional);
    }

    @Transactional
    public ProfissionalDTO createProfissional(ProfissionalDTO profissionalDTO) {
        if (profissionalRepository.count() >= 20) {
            throw new BadRequestException("Limite de 20 profissionais atingido para demonstração.");
        }
        Profissional profissional = profissionalMapper.toEntity(profissionalDTO);

        if (profissionalDTO.getUserId() != null) {
            User user = userRepository.findById(profissionalDTO.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
            profissional.setUser(user);
        }

        Profissional savedProfissional = profissionalRepository.save(profissional);
        return profissionalMapper.toDto(savedProfissional);
    }

    @Transactional
    public ProfissionalDTO updateProfissional(Long id, ProfissionalDTO profissionalDTO) {
        if (!profissionalRepository.existsById(id)) {
            throw new ResourceNotFoundException("Profissional não encontrado");
        }

        Profissional profissional = profissionalMapper.toEntity(profissionalDTO);
        profissional.setId(id);

        if (profissionalDTO.getUserId() != null) {
            User user = userRepository.findById(profissionalDTO.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
            profissional.setUser(user);
        }

        Profissional updatedProfissional = profissionalRepository.save(profissional);
        return profissionalMapper.toDto(updatedProfissional);
    }

    @Transactional
    public void deleteProfissional(Long id) {
        if (!profissionalRepository.existsById(id)) {
            throw new ResourceNotFoundException("Profissional não encontrado");
        }
        profissionalRepository.deleteById(id);
    }

    public List<Profissional> getProfissionaisAtivos() {
        return profissionalRepository.findByStatus(Profissional.StatusProfissional.DISPONIVEL);
    }

    @Transactional
    public void desativarProfissional(Long profissionalId) {
        Profissional profissional = profissionalRepository.findById(profissionalId)
            .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));
        profissional.setStatus(StatusProfissional.INATIVO);
        profissionalRepository.save(profissional);

        // Desativa todas as escalas futuras
        List<Escala> escalas = escalaRepository.findByProfissionalIdAndDataAfter(profissionalId, LocalDate.now().minusDays(1));
        for (Escala escala : escalas) {
            escala.setStatus(Escala.StatusEscala.CANCELADA);
            escalaRepository.save(escala);
        }
    }

    @Transactional
    public void reativarProfissional(Long profissionalId) {
        Profissional profissional = profissionalRepository.findById(profissionalId)
            .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));
        profissional.setStatus(StatusProfissional.DISPONIVEL);
        profissionalRepository.save(profissional);
        // Não reativa escalas antigas
    }
}
