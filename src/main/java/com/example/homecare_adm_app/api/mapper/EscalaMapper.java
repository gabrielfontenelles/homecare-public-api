package com.example.homecare_adm_app.api.mapper;

import com.example.homecare_adm_app.api.dto.EscalaDTO;
import com.example.homecare_adm_app.api.model.Escala;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EscalaMapper {

    @Mapping(source = "profissional.id", target = "profissionalId")
    @Mapping(source = "profissional.nome", target = "nomeProfissional")
    @Mapping(source = "paciente.nome", target = "nomePaciente")
    @Mapping(target = "profissional", ignore = true)
    EscalaDTO toDto(Escala escala);

    @Mapping(target = "profissional", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Escala toEntity(EscalaDTO escalaDTO);

    @Mapping(target = "profissional", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEscalaFromDto(EscalaDTO escalaDTO, @MappingTarget Escala escala);
}