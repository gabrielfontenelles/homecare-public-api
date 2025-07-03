package com.example.homecare_adm_app.api.mapper;


import com.example.homecare_adm_app.api.dto.UserDTO;
import com.example.homecare_adm_app.api.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserConfigMapper.class})
public interface UserMapper {

    @Mapping(source = "userConfig", target = "config")
    @Mapping(source = "senha", target = "senha", ignore = true)
    @Mapping(source = "id", target = "id")
    @Mapping(source = "nome", target = "nome")
    @Mapping(source = "email", target = "email")
    @Mapping(source = "telefone", target = "telefone")
    @Mapping(source = "lastLogin", target = "lastLogin")
    @Mapping(source = "isActive", target = "isActive")
    @Mapping(source = "roles", target = "roles")
    UserDTO toDto(User user);

    @Mapping(source = "config", target = "userConfig")
    @Mapping(target = "resetPasswordToken", ignore = true)
    @Mapping(target = "resetPasswordExpiry", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    User toEntity(UserDTO userDTO);

    @Mapping(source = "config", target = "userConfig")
    @Mapping(target = "resetPasswordToken", ignore = true)
    @Mapping(target = "resetPasswordExpiry", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "senha", ignore = true)
    void updateUserFromDto(UserDTO userDTO, @MappingTarget User user);
}