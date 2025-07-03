package com.example.homecare_adm_app.api.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PacienteDTO {

    private Long id;
    private String nome;
    private LocalDate dataNascimento;
    private String cpf;
    private String rg;
    private String telefone;
    private String email;
    private String endereco;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;
    private String nomeResponsavel;
    private String telefoneResponsavel;
    private String observacoes;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

   
    private List<AgendamentoDTO> agendamentos;
}