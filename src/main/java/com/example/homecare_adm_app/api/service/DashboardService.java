package com.example.homecare_adm_app.api.service;


import com.example.homecare_adm_app.api.dto.DashboardDTO;
import com.example.homecare_adm_app.api.model.Agendamento;
import com.example.homecare_adm_app.api.model.Paciente;
import com.example.homecare_adm_app.api.model.Profissional;
import com.example.homecare_adm_app.api.repository.AgendamentoRepository;
import com.example.homecare_adm_app.api.repository.PacienteRepository;
import com.example.homecare_adm_app.api.repository.ProfissionalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final AgendamentoRepository agendamentoRepository;

    public DashboardDTO getDashboardData() {
        
        long pacientesAtivos = pacienteRepository.countByStatus(Paciente.StatusPaciente.ATIVO);

        long visitasAgendadasHoje = agendamentoRepository.countByData(LocalDate.now());

        
        long profissionaisDisponiveis = profissionalRepository.countByStatus(Profissional.StatusProfissional.DISPONIVEL);
        long profissionaisCadastrados = profissionalRepository.count();

       
        long totalVisitas = agendamentoRepository.count();

       
        List<DashboardDTO.VisitasPorDiaDTO> visitasUltimos30Dias = getVisitasUltimos30Dias();

       
        List<DashboardDTO.PacienteRecenteDTO> pacientesRecentes = getPacientesRecentes();

        return DashboardDTO.builder()
                .pacientesAtivos(pacientesAtivos)
                .visitasAgendadasHoje(visitasAgendadasHoje)
                .totalVisitas(totalVisitas)
                .profissionais(DashboardDTO.ProfissionaisCountDTO.builder()
                        .disponiveis(profissionaisDisponiveis)
                        .cadastrados(profissionaisCadastrados)
                        .build())
                .visitasUltimos30Dias(visitasUltimos30Dias)
                .pacientesRecentes(pacientesRecentes)
                .build();
    }

    private List<DashboardDTO.VisitasPorDiaDTO> getVisitasUltimos30Dias() {
        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.minusDays(29);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd");

        List<DashboardDTO.VisitasPorDiaDTO> resultado = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            LocalDate data = inicio.plusDays(i);
            long visitas = agendamentoRepository.countByData(data);
            resultado.add(DashboardDTO.VisitasPorDiaDTO.builder()
                    .dia(data.format(formatter))
                    .visitas(visitas)
                    .build());
        }

        return resultado;
    }

    private List<DashboardDTO.PacienteRecenteDTO> getPacientesRecentes() {
        List<Paciente> pacientes = pacienteRepository.findTop5ByOrderByCreatedAtDesc();

        return pacientes.stream()
                .map(paciente -> {
                    String especialidade = agendamentoRepository.findFirstByPacienteIdOrderByDataDesc(paciente.getId())
                            .map(Agendamento::getTipo)
                            .orElse("Não definida");

                    return DashboardDTO.PacienteRecenteDTO.builder()
                            .id(paciente.getId())
                            .nome(paciente.getNome())
                            .especialidade(especialidade)
                            .status(paciente.getStatus().name())
                            .build();
                })
                .collect(Collectors.toList());
    }
}