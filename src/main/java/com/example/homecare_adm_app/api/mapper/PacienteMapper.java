package com.example.homecare_adm_app.api.mapper;


import com.example.homecare_adm_app.api.dto.PacienteDTO;
import com.example.homecare_adm_app.api.model.Paciente;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PacienteMapper {

    @Mapping(target = "agendamentos", ignore = true)
    PacienteDTO toDto(Paciente paciente);

    @Mapping(target = "agendamentos", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Paciente toEntity(PacienteDTO pacienteDTO);

    @Mapping(target = "agendamentos", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updatePacienteFromDto(PacienteDTO pacienteDTO, @MappingTarget Paciente paciente);
}