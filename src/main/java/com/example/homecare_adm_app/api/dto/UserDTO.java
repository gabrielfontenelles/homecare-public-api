package com.example.homecare_adm_app.api.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String nome;
    private String email;
    private String senha;
    private String telefone;
    private LocalDateTime lastLogin;
    private boolean isActive; 
    private Set<String> roles;

    // Referência para configurações do usuário
    private UserConfigDTO config;
}
