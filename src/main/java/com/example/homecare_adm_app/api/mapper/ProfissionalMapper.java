package com.example.homecare_adm_app.api.mapper;



import com.example.homecare_adm_app.api.dto.ProfissionalDTO;
import com.example.homecare_adm_app.api.model.Profissional;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProfissionalMapper {

    @Mapping(target = "escalas", ignore = true)
    @Mapping(target = "agendamentos", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(source = "user.id", target = "userId")
    ProfissionalDTO toDto(Profissional profissional);

    @Mapping(target = "escalas", ignore = true)
    @Mapping(target = "agendamentos", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Profissional toEntity(ProfissionalDTO profissionalDTO);

    @Mapping(target = "escalas", ignore = true)
    @Mapping(target = "agendamentos", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateProfissionalFromDto(ProfissionalDTO profissionalDTO, @MappingTarget Profissional profissional);
}