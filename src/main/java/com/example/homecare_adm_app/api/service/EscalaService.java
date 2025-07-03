package com.example.homecare_adm_app.api.service;


import com.example.homecare_adm_app.api.dto.EscalaDTO;
import com.example.homecare_adm_app.api.exeption.BadRequestException;
import com.example.homecare_adm_app.api.exeption.ResourceNotFoundException;
import com.example.homecare_adm_app.api.mapper.EscalaMapper;
import com.example.homecare_adm_app.api.model.Escala;
import com.example.homecare_adm_app.api.model.Profissional;
import com.example.homecare_adm_app.api.model.Paciente;
import com.example.homecare_adm_app.api.repository.EscalaRepository;
import com.example.homecare_adm_app.api.repository.ProfissionalRepository;
import com.example.homecare_adm_app.api.repository.PacienteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EscalaService {

    private final EscalaRepository escalaRepository;
    private final ProfissionalRepository profissionalRepository;
    private final PacienteRepository pacienteRepository;
    private final EscalaMapper escalaMapper;

    public List<EscalaDTO> getEscalasByDate(LocalDate data) {
        List<Escala> escalas = escalaRepository.findByData(data);
        return escalas.stream()
                .map(escalaMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<EscalaDTO> getEscalasByProfissional(Long profissionalId, LocalDate dataInicio, LocalDate dataFim) {
        if (!profissionalRepository.existsById(profissionalId)) {
            throw new ResourceNotFoundException("Profissional não encontrado");
        }

        List<Escala> escalas;
        if (dataInicio != null && dataFim != null) {
            escalas = escalaRepository.findByProfissionalIdAndDataBetween(profissionalId, dataInicio, dataFim);
        } else {
            escalas = escalaRepository.findByProfissionalId(profissionalId);
        }

        return escalas.stream()
                .map(escalaMapper::toDto)
                .collect(Collectors.toList());
    }

    public EscalaDTO getEscalaById(Long id) {
        Escala escala = escalaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Escala não encontrada"));
        return escalaMapper.toDto(escala);
    }

    @Transactional
    public EscalaDTO createEscala(EscalaDTO escalaDTO) {
        validarEscala(escalaDTO);

        Profissional profissional = profissionalRepository.findById(escalaDTO.getProfissionalId())
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));
        Paciente paciente = pacienteRepository.findById(escalaDTO.getPacienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

        Escala escala = escalaMapper.toEntity(escalaDTO);
        escala.setProfissional(profissional);
        escala.setPaciente(paciente);
        Escala savedEscala = escalaRepository.save(escala);
        return escalaMapper.toDto(savedEscala);
    }

    @Transactional
    public EscalaDTO updateEscala(Long id, EscalaDTO escalaDTO) {
        if (!escalaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Escala não encontrada");
        }

        Profissional profissional = profissionalRepository.findById(escalaDTO.getProfissionalId())
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));
        Paciente paciente = pacienteRepository.findById(escalaDTO.getPacienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

        Escala escala = escalaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Escala não encontrada"));

        
        escala.setProfissional(profissional);
        escala.setPaciente(paciente);
        escala.setData(escalaDTO.getData());
        escala.setHoraInicio(escalaDTO.getHoraInicio());
        escala.setHoraFim(escalaDTO.getHoraFim());
        escala.setStatus(Escala.StatusEscala.valueOf(escalaDTO.getStatus()));
        escala.setObservacoes(escalaDTO.getObservacoes());

        Escala updatedEscala = escalaRepository.save(escala);
        return escalaMapper.toDto(updatedEscala);
    }

    @Transactional
    public void deleteEscala(Long id) {
        if (!escalaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Escala não encontrada");
        }
        escalaRepository.deleteById(id);
    }

    private void validarEscala(EscalaDTO escalaDTO) {
        // Comentado para permitir escalas noturnas (ex: 19:00 às 07:00)
        // if (escalaDTO.getHoraInicio().isAfter(escalaDTO.getHoraFim())) {
        //     throw new BadRequestException("Hora de início deve ser anterior à hora de fim");
        // }

        // Verificar sobreposição de escalas para o mesmo profissional
        List<Escala> escalasExistentes = escalaRepository.findByProfissionalIdAndData(
                escalaDTO.getProfissionalId(), escalaDTO.getData());

        for (Escala escala : escalasExistentes) {
            if (escalaDTO.getId() != null && escala.getId().equals(escalaDTO.getId())) {
                continue; // Ignorar a própria escala ao atualizar
            }

            if (horariosSobrepostos(
                escalaDTO.getHoraInicio(),
                escalaDTO.getHoraFim(),
                escala.getHoraInicio(),
                escala.getHoraFim()
            )) {
                throw new BadRequestException("Existe sobreposição com outra escala do profissional");
            }
        }
    }

    private boolean horariosSobrepostos(LocalTime inicio1, LocalTime fim1, LocalTime inicio2, LocalTime fim2) {
       
        int start1 = inicio1.toSecondOfDay() / 60;
        int end1 = fim1.toSecondOfDay() / 60;
        int start2 = inicio2.toSecondOfDay() / 60;
        int end2 = fim2.toSecondOfDay() / 60;

        // Se o fim for menor que o início, significa que passa da meia-noite
        // Nesse caso, adicionamos 24 horas (1440 minutos)
        if (end1 <= start1) end1 += 24 * 60;
        if (end2 <= start2) end2 += 24 * 60;

        
        return (start1 < end2 && end1 > start2);
    }

   
    @Scheduled(cron = "0 * * * * *") // a cada minuto
    @Transactional
    public void atualizarStatusEscalasAutomaticamente() {
        List<Escala> escalas = escalaRepository.findAll();
        LocalDateTime agora = LocalDateTime.now();
        for (Escala escala : escalas) {
            LocalDateTime inicio = escala.getData().atTime(escala.getHoraInicio());
            LocalDateTime fim = escala.getData().atTime(escala.getHoraFim());
            if (escala.getHoraFim().isBefore(escala.getHoraInicio()) || escala.getHoraFim().equals(escala.getHoraInicio())) {
                fim = fim.plusDays(1);
            }
            Escala.StatusEscala novoStatus = escala.getStatus();
            if (agora.isAfter(fim)) {
                novoStatus = Escala.StatusEscala.COMPLETA;
            } else if (!agora.isBefore(inicio) && !agora.isAfter(fim)) {
                novoStatus = Escala.StatusEscala.COBERTA;
            } else if (agora.isBefore(inicio)) {
                novoStatus = Escala.StatusEscala.ABERTA;
            }
            if (escala.getStatus() != novoStatus) {
                escala.setStatus(novoStatus);
                escalaRepository.save(escala);
            }
        }
    }
}
