package com.example.homecare_adm_app.api.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgendamentoDTO {

    private Long id;
    private Long pacienteId;
    private String nomePaciente;
    private Long profissionalId;
    private String nomeProfissional;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private String tipo;
    private String status;
    private String observacoes;
    private String enderecoAtendimento;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String modalidade;
    private String especialidade;

    // Ignorados pelo mapper, mas necessários para as referências
    private PacienteDTO paciente;
    private ProfissionalDTO profissional;
}
