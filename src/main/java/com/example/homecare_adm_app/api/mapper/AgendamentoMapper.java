package com.example.homecare_adm_app.api.mapper;


import com.example.homecare_adm_app.api.dto.AgendamentoDTO;
import com.example.homecare_adm_app.api.model.Agendamento;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AgendamentoMapper {

    @Mapping(source = "paciente.id", target = "pacienteId")
    @Mapping(source = "paciente.nome", target = "nomePaciente")
    @Mapping(source = "profissional.id", target = "profissionalId")
    @Mapping(target = "nomeProfissional", expression = "java(agendamento.getProfissional() != null ? agendamento.getProfissional().getNome() : agendamento.getNomeProfissional())")
    @Mapping(source = "modalidade", target = "modalidade")
    @Mapping(source = "especialidade", target = "especialidade")
    @Mapping(target = "paciente", ignore = true)
    @Mapping(target = "profissional", ignore = true)
    AgendamentoDTO toDto(Agendamento agendamento);

    @Mapping(target = "paciente", ignore = true)
    @Mapping(target = "profissional", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(source = "nomeProfissional", target = "nomeProfissional")
    Agendamento toEntity(AgendamentoDTO agendamentoDTO);

    @Mapping(target = "paciente", ignore = true)
    @Mapping(target = "profissional", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateAgendamentoFromDto(AgendamentoDTO agendamentoDTO, @MappingTarget Agendamento agendamento);
}
