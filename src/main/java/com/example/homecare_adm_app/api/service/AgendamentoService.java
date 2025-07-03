package com.example.homecare_adm_app.api.service;


import com.example.homecare_adm_app.api.dto.AgendamentoDTO;
import com.example.homecare_adm_app.api.exeption.BadRequestException;
import com.example.homecare_adm_app.api.exeption.ResourceNotFoundException;
import com.example.homecare_adm_app.api.mapper.AgendamentoMapper;
import com.example.homecare_adm_app.api.model.Agendamento;
import com.example.homecare_adm_app.api.model.Escala;
import com.example.homecare_adm_app.api.model.Paciente;
import com.example.homecare_adm_app.api.model.Profissional;
import com.example.homecare_adm_app.api.repository.AgendamentoRepository;
import com.example.homecare_adm_app.api.repository.EscalaRepository;
import com.example.homecare_adm_app.api.repository.PacienteRepository;
import com.example.homecare_adm_app.api.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgendamentoService {

    private final AgendamentoRepository agendamentoRepository;
    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final EscalaRepository escalaRepository;
    private final AgendamentoMapper agendamentoMapper;

    public List<AgendamentoDTO> getAgendamentosByDate(LocalDate data) {
        List<Agendamento> agendamentos = agendamentoRepository.findByData(data);
        return agendamentos.stream()
                .map(agendamentoMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<AgendamentoDTO> getAgendamentosHoje() {
        return getAgendamentosByDate(LocalDate.now());
    }

    public List<AgendamentoDTO> getAgendamentosByPaciente(Long pacienteId, LocalDate dataInicio, LocalDate dataFim) {
        if (!pacienteRepository.existsById(pacienteId)) {
            throw new ResourceNotFoundException("Paciente não encontrado");
        }

        List<Agendamento> agendamentos;
        if (dataInicio != null && dataFim != null) {
            agendamentos = agendamentoRepository.findByPacienteIdAndDataBetween(pacienteId, dataInicio, dataFim);
        } else {
            agendamentos = agendamentoRepository.findByPacienteId(pacienteId);
        }

        return agendamentos.stream()
                .map(agendamentoMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<AgendamentoDTO> getAgendamentosByProfissional(Long profissionalId, LocalDate dataInicio, LocalDate dataFim) {
        if (!profissionalRepository.existsById(profissionalId)) {
            throw new ResourceNotFoundException("Profissional não encontrado");
        }

        List<Agendamento> agendamentos;
        if (dataInicio != null && dataFim != null) {
            agendamentos = agendamentoRepository.findByProfissionalIdAndDataBetween(profissionalId, dataInicio, dataFim);
        } else {
            agendamentos = agendamentoRepository.findByProfissionalId(profissionalId);
        }

        return agendamentos.stream()
                .map(agendamentoMapper::toDto)
                .collect(Collectors.toList());
    }

    public AgendamentoDTO getAgendamentoById(Long id) {
        Agendamento agendamento = agendamentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado"));
        return agendamentoMapper.toDto(agendamento);
    }

    @Transactional
    public AgendamentoDTO createAgendamento(AgendamentoDTO agendamentoDTO) {
        if (agendamentoRepository.count() >= 20) {
            throw new BadRequestException("Limite de 20 agendamentos atingido para demonstração.");
        }
        if (agendamentoDTO.getStatus() == null) {
            agendamentoDTO.setStatus("PENDENTE");
        }
        validarAgendamento(agendamentoDTO);

        Paciente paciente = pacienteRepository.findById(agendamentoDTO.getPacienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));

        Profissional profissional = null;
        Long profissionalId = agendamentoDTO.getProfissionalId();
        if (profissionalId != null) {
            profissional = profissionalRepository.findById(profissionalId)
                    .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));
            verificarDisponibilidadeProfissional(agendamentoDTO);
        }

        Agendamento agendamento = agendamentoMapper.toEntity(agendamentoDTO);
        agendamento.setPaciente(paciente);
        agendamento.setProfissional(profissional);
        if (profissional == null && agendamentoDTO.getNomeProfissional() != null) {
            agendamento.setNomeProfissional(agendamentoDTO.getNomeProfissional());
        } else {
            agendamento.setNomeProfissional(null);
        }
        Agendamento savedAgendamento = agendamentoRepository.save(agendamento);
        return agendamentoMapper.toDto(savedAgendamento);
    }

    @Transactional
    public AgendamentoDTO updateAgendamento(Long id, AgendamentoDTO agendamentoDTO) {
        if (id == null) {
            throw new BadRequestException("O id do agendamento não pode ser nulo.");
        }
        Optional<Agendamento> agendamentoOpt = agendamentoRepository.findById(id);
        if (agendamentoOpt.isEmpty()) {
            throw new ResourceNotFoundException("Agendamento não encontrado");
        }
        Agendamento agendamento = agendamentoOpt.get();

        // Atualize os campos do agendamento com base no DTO
        if (agendamentoDTO.getPacienteId() != null) {
            Paciente paciente = pacienteRepository.findById(agendamentoDTO.getPacienteId())
                    .orElseThrow(() -> new ResourceNotFoundException("Paciente não encontrado"));
            agendamento.setPaciente(paciente);
        } else { // Adicionado para garantir que paciente não fique nulo se não vier no DTO
             throw new BadRequestException("Paciente não pode ser nulo");
        }

        agendamento.setData(agendamentoDTO.getData());
        agendamento.setHoraInicio(agendamentoDTO.getHoraInicio());
        agendamento.setHoraFim(agendamentoDTO.getHoraFim());
        agendamento.setTipo(agendamentoDTO.getTipo());

        if (agendamentoDTO.getStatus() != null) {
            agendamento.setStatus(Agendamento.StatusAgendamento.valueOf(agendamentoDTO.getStatus().toUpperCase()));
        }

        agendamento.setObservacoes(agendamentoDTO.getObservacoes());
        agendamento.setEnderecoAtendimento(agendamentoDTO.getEnderecoAtendimento());
        agendamento.setModalidade(agendamentoDTO.getModalidade());
        agendamento.setEspecialidade(agendamentoDTO.getEspecialidade());

       
        if (agendamentoDTO.getProfissionalId() != null) {
            
            Profissional profissional = profissionalRepository.findById(agendamentoDTO.getProfissionalId())
                    .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));
            agendamento.setProfissional(profissional);
            agendamento.setNomeProfissional(null); 

        } else if (agendamentoDTO.getNomeProfissional() != null && !agendamentoDTO.getNomeProfissional().trim().isEmpty()) {
            
            agendamento.setProfissional(null); 
            agendamento.setNomeProfissional(agendamentoDTO.getNomeProfissional().trim());
        } else {
           
            agendamento.setProfissional(null);
            agendamento.setNomeProfissional(null);
        }

        validarAgendamento(agendamentoDTO);
        agendamentoRepository.save(agendamento);
        return agendamentoMapper.toDto(agendamento);
    }

    @Transactional
    public void deleteAgendamento(Long id) {
        if (!agendamentoRepository.existsById(id)) {
            throw new ResourceNotFoundException("Agendamento não encontrado");
        }
        agendamentoRepository.deleteById(id);
    }

    private void validarAgendamento(AgendamentoDTO agendamentoDTO) {
        if (agendamentoDTO.getHoraInicio().isAfter(agendamentoDTO.getHoraFim())) {
            throw new BadRequestException("Hora de início deve ser anterior à hora de fim");
        }

        // Verificar sobreposição para profissional, se profissionalId não for nulo
        if (agendamentoDTO.getProfissionalId() != null) {
            List<Agendamento> sobreposicoesProfissional = agendamentoRepository
                    .findSobreposicoesProfissional(
                            agendamentoDTO.getProfissionalId(),
                            agendamentoDTO.getData(),
                            agendamentoDTO.getHoraInicio(),
                            agendamentoDTO.getHoraFim(),
                            agendamentoDTO.getId());

            if (!sobreposicoesProfissional.isEmpty()) {
                
                System.out.println("Sobreposição de horário para profissional detectada:");
                sobreposicoesProfissional.forEach(ag -> System.out.println(" - Agendamento ID: " + ag.getId() + ", Hora: " + ag.getHoraInicio() + "-" + ag.getHoraFim()));
                throw new BadRequestException("Existe sobreposição de horário para este profissional");
            }
        }

       
        List<Agendamento> sobreposicoesPaciente = agendamentoRepository
                .findSobreposicoesPaciente(
                        agendamentoDTO.getPacienteId(),
                        agendamentoDTO.getData(),
                        agendamentoDTO.getHoraInicio(),
                        agendamentoDTO.getHoraFim(),
                        agendamentoDTO.getId());

        if (!sobreposicoesPaciente.isEmpty()) {
            
            System.out.println("Sobreposição de horário para paciente detectada:");
            sobreposicoesPaciente.forEach(ag -> System.out.println(" - Agendamento ID: " + ag.getId() + ", Hora: " + ag.getHoraInicio() + "-" + ag.getHoraFim()));
            throw new BadRequestException("Existe sobreposição com outro agendamento do paciente");
        }
    }

    private void verificarDisponibilidadeProfissional(AgendamentoDTO agendamentoDTO) {
        if (agendamentoDTO.getProfissionalId() == null) {
            return;
        }

        Profissional profissional = profissionalRepository.findById(agendamentoDTO.getProfissionalId())
                .orElseThrow(() -> new ResourceNotFoundException("Profissional não encontrado"));

       
        if (!"Técnico de Enfermagem".equalsIgnoreCase(profissional.getEspecialidade())) {
            return;
        }

        List<Escala> escalas = escalaRepository.findByProfissionalIdAndData(
                agendamentoDTO.getProfissionalId(), agendamentoDTO.getData());

        if (escalas.isEmpty()) {
            throw new BadRequestException("Profissional não possui escala para esta data");
        }

        boolean horarioDisponivel = false;
        for (Escala escala : escalas) {
            if ((agendamentoDTO.getHoraInicio().equals(escala.getHoraInicio()) ||
                    agendamentoDTO.getHoraInicio().isAfter(escala.getHoraInicio())) &&
                    (agendamentoDTO.getHoraFim().equals(escala.getHoraFim()) ||
                            agendamentoDTO.getHoraFim().isBefore(escala.getHoraFim()))) {
                horarioDisponivel = true;
                break;
            }
        }

        if (!horarioDisponivel) {
            throw new BadRequestException("Horário fora da escala do profissional");
        }
    }

    @Scheduled(cron = "0 */5 * * * *") // a cada 5 minutos
    public void atualizarAgendamentosParaRealizado() {
        LocalDateTime agora = LocalDateTime.now();
        LocalDate hoje = agora.toLocalDate();
        LocalTime hora = agora.toLocalTime();
        var agendamentos = agendamentoRepository.findPendentesOuConfirmadosAte(hoje, hora);
        for (var ag : agendamentos) {
            ag.setStatus(Agendamento.StatusAgendamento.REALIZADO);
            agendamentoRepository.save(ag);
        }
    }
}