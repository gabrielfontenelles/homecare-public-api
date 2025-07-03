package com.example.homecare_adm_app.api.mapper;



import com.example.homecare_adm_app.api.dto.UserConfigDTO;
import com.example.homecare_adm_app.api.model.UserConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserConfigMapper {

    @Mapping(target = "user", ignore = true)
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "theme", target = "theme")
    @Mapping(source = "notificationsEnabled", target = "notificationsEnabled")
    @Mapping(source = "emailNotifications", target = "emailNotifications")
    UserConfigDTO toDto(UserConfig userConfig);

    @Mapping(target = "user", ignore = true)
    UserConfig toEntity(UserConfigDTO userConfigDTO);

    @Mapping(target = "user", ignore = true)
    void updateUserConfigFromDto(UserConfigDTO userConfigDTO, @MappingTarget UserConfig userConfig);
}