package com.example.homecare_adm_app.api.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfissionalDTO {

    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String cpf;
    private String especialidade;
    private String registroProfissional;
    private String status;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

   
    private UserDTO user;
    private List<EscalaDTO> escalas;
    private List<AgendamentoDTO> agendamentos;
}
