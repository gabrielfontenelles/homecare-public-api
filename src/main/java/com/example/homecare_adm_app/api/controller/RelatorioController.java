package com.example.homecare_adm_app.api.controller;


import com.example.homecare_adm_app.api.service.RelatorioService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/relatorios")
@RequiredArgsConstructor
public class RelatorioController {

    private final RelatorioService relatorioService;

    @GetMapping("/pacientes")
    public ResponseEntity<byte[]> gerarRelatorioPacientes(
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "pdf") String formato
    ) {
        byte[] relatorio;
        String filename;
        MediaType mediaType;

        if ("pdf".equalsIgnoreCase(formato)) {
            relatorio = relatorioService.gerarRelatorioPacientesPDF(status);
            filename = "relatorio_pacientes.pdf";
            mediaType = MediaType.APPLICATION_PDF;
        } else if ("json".equalsIgnoreCase(formato)) {
            relatorio = relatorioService.gerarRelatorioPacientesJSON(status);
            filename = "relatorio_pacientes.json";
            mediaType = MediaType.APPLICATION_JSON;
        } else if ("csv".equalsIgnoreCase(formato)) {
            relatorio = relatorioService.gerarRelatorioPacientesCSV(status);
            filename = "relatorio_pacientes.csv";
            mediaType = MediaType.parseMediaType("text/csv");
        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(relatorio);
    }

    @GetMapping("/profissionais")
    public ResponseEntity<byte[]> gerarRelatorioProfissionais(
            @RequestParam(required = false) String especialidade,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String formato
    ) {
        byte[] relatorio;
        String filename;
        MediaType mediaType;

        if ("pdf".equalsIgnoreCase(formato)) {
            relatorio = relatorioService.gerarRelatorioProfissionaisPDF(especialidade, status);
            filename = "relatorio_profissionais.pdf";
            mediaType = MediaType.APPLICATION_PDF;
        } else {
            relatorio = relatorioService.gerarRelatorioProfissionaisJSON(especialidade, status);
            filename = "relatorio_profissionais.json";
            mediaType = MediaType.APPLICATION_JSON;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(relatorio);
    }

    @GetMapping("/agendamentos")
    public ResponseEntity<byte[]> gerarRelatorioAgendamentos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "pdf") String formato
    ) {
        byte[] relatorio;
        String filename;
        MediaType mediaType;

        if ("pdf".equalsIgnoreCase(formato)) {
            relatorio = relatorioService.gerarRelatorioAgendamentosPDF(dataInicio, dataFim, status);
            filename = "relatorio_agendamentos.pdf";
            mediaType = MediaType.APPLICATION_PDF;
        } else if ("json".equalsIgnoreCase(formato)) {
            relatorio = relatorioService.gerarRelatorioAgendamentosJSON(dataInicio, dataFim, status);
            filename = "relatorio_agendamentos.json";
            mediaType = MediaType.APPLICATION_JSON;
        } else if ("csv".equalsIgnoreCase(formato)) {
            relatorio = relatorioService.gerarRelatorioAgendamentosCSV(dataInicio, dataFim, status);
            filename = "relatorio_agendamentos.csv";
            mediaType = MediaType.parseMediaType("text/csv");
        } else {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(relatorio);
    }

    @GetMapping("/escalas")
    public ResponseEntity<byte[]> gerarRelatorioEscalas(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
            @RequestParam(required = false) Long profissionalId,
            @RequestParam(required = false) String especialidade,
            @RequestParam(required = false) String status,
            @RequestParam(required = false, defaultValue = "pdf") String formato
    ) {
        byte[] relatorio;
        String filename;
        MediaType mediaType;

        if ("pdf".equalsIgnoreCase(formato)) {
            relatorio = relatorioService.gerarRelatorioEscalasPDF(dataInicio, dataFim, profissionalId, especialidade, status);
            filename = "relatorio_escalas.pdf";
            mediaType = MediaType.APPLICATION_PDF;
        } else if ("json".equalsIgnoreCase(formato)) {
            relatorio = relatorioService.gerarRelatorioEscalasJSON(dataInicio, dataFim, profissionalId, especialidade, status);
            filename = "relatorio_escalas.json";
            mediaType = MediaType.APPLICATION_JSON;
        } else if ("csv".equalsIgnoreCase(formato)) {
            relatorio = relatorioService.gerarRelatorioEscalasCSV(dataInicio, dataFim, profissionalId, especialidade, status);
            filename = "relatorio_escalas.csv";
            mediaType = MediaType.parseMediaType("text/csv");
        } else {
            return ResponseEntity.badRequest().build(); // Formato inválido
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(relatorio);
    }
}
