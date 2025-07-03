package com.example.homecare_adm_app.api.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDTO {

    private long pacientesAtivos;
    private long visitasAgendadasHoje;
    private ProfissionaisCountDTO profissionais;
    private List<VisitasPorDiaDTO> visitasUltimos30Dias;
    private List<PacienteRecenteDTO> pacientesRecentes;
    private long totalVisitas;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfissionaisCountDTO {
        private long disponiveis;
        private long cadastrados;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisitasPorDiaDTO {
        private String dia;
        private long visitas;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PacienteRecenteDTO {
        private Long id;
        private String nome;
        private String especialidade;
        private String status;
    }
}
