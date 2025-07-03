package com.example.homecare_adm_app.api.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.homecare_adm_app.api.dto.AgendamentoDTO;
import com.example.homecare_adm_app.api.dto.PacienteDTO;
import com.example.homecare_adm_app.api.dto.ProfissionalDTO;
import com.example.homecare_adm_app.api.mapper.AgendamentoMapper;
import com.example.homecare_adm_app.api.mapper.PacienteMapper;
import com.example.homecare_adm_app.api.mapper.ProfissionalMapper;
import com.example.homecare_adm_app.api.model.Agendamento;
import com.example.homecare_adm_app.api.model.Paciente;
import com.example.homecare_adm_app.api.model.Profissional;
import com.example.homecare_adm_app.api.repository.AgendamentoRepository;
import com.example.homecare_adm_app.api.repository.PacienteRepository;
import com.example.homecare_adm_app.api.repository.ProfissionalRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.example.homecare_adm_app.api.model.Escala;
import com.example.homecare_adm_app.api.repository.EscalaRepository;
import com.example.homecare_adm_app.api.dto.EscalaDTO;
import com.example.homecare_adm_app.api.mapper.EscalaMapper;


@Service
@RequiredArgsConstructor
public class RelatorioService {

    private final PacienteRepository pacienteRepository;
    private final ProfissionalRepository profissionalRepository;
    private final AgendamentoRepository agendamentoRepository;
    private final PacienteMapper pacienteMapper;
    private final ProfissionalMapper profissionalMapper;
    private final AgendamentoMapper agendamentoMapper;
    private final ObjectMapper objectMapper;
    private final EscalaRepository escalaRepository;
    private final EscalaMapper escalaMapper;

    public byte[] gerarRelatorioPacientesPDF(String status) {
        List<Paciente> pacientes = buscarPacientes(status);
        List<PacienteDTO> pacientesDTO = pacientes.stream()
                .map(pacienteMapper::toDto)
                .collect(Collectors.toList());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
            Paragraph title = new Paragraph("Relatório de Pacientes", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            Font filterFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.DARK_GRAY);
            Paragraph filters = new Paragraph("Filtros: " + (status != null ? "Status = " + status : "Todos"), filterFont);
            document.add(filters);
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 3, 1, 2, 1});

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
            BaseColor headerColor = new BaseColor(0, 150, 76);

            addTableHeader(table, headerFont, headerColor, "ID", "Nome", "Idade", "Telefone", "Status");

            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (PacienteDTO paciente : pacientesDTO) {
                table.addCell(createCell(paciente.getId().toString(), dataFont, Element.ALIGN_CENTER));
                table.addCell(createCell(paciente.getNome(), dataFont, Element.ALIGN_LEFT));

                String idade = paciente.getDataNascimento() != null
                        ? calcularIdade(paciente.getDataNascimento()) + " anos"
                        : "N/A";
                table.addCell(createCell(idade, dataFont, Element.ALIGN_CENTER));

                table.addCell(createCell(paciente.getTelefone() != null ? paciente.getTelefone() : "N/A", dataFont, Element.ALIGN_LEFT));
                table.addCell(createCell(paciente.getStatus().toString(), dataFont, Element.ALIGN_CENTER));
            }

            document.add(table);
            document.add(Chunk.NEWLINE);

            Font footerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, BaseColor.GRAY);
            Paragraph footer = new Paragraph("Relatório gerado em " + LocalDate.now().format(formatter) + " - HomeCare Coop", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório PDF de pacientes", e);
        }
    }

    public byte[] gerarRelatorioPacientesJSON(String status) {
        List<Paciente> pacientes = buscarPacientes(status);
        List<PacienteDTO> pacientesDTO = pacientes.stream()
                .map(pacienteMapper::toDto)
                .collect(Collectors.toList());

        try {
            return objectMapper.writeValueAsBytes(pacientesDTO);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório JSON de pacientes", e);
        }
    }

    public byte[] gerarRelatorioPacientesCSV(String status) {
        List<Paciente> pacientes = buscarPacientes(status);
        List<PacienteDTO> pacientesDTO = pacientes.stream()
                .map(pacienteMapper::toDto)
                .collect(Collectors.toList());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(osw)) {

            writer.println("ID,Nome,Data Nascimento,CPF,Telefone,Endereco,Status,Observacoes");

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (PacienteDTO paciente : pacientesDTO) {
                writer.printf("%d,\"%s\",%s,\"%s\",\"%s\",\"%s\",%s,\"%s\"%n",
                        paciente.getId(),
                        escapeCsv(paciente.getNome()),
                        paciente.getDataNascimento() != null ? paciente.getDataNascimento().format(dateFormatter) : "",
                        escapeCsv(paciente.getCpf()),
                        escapeCsv(paciente.getTelefone()),
                        escapeCsv(paciente.getEndereco()),
                        paciente.getStatus() != null ? paciente.getStatus().toString() : "",
                        escapeCsv(paciente.getObservacoes())
                );
            }

            writer.flush();
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar relatório CSV de pacientes", e);
        }
    }

    public byte[] gerarRelatorioProfissionaisPDF(String especialidade, String status) {
        List<Profissional> profissionais = buscarProfissionais(especialidade, status);
        List<ProfissionalDTO> profissionaisDTO = profissionais.stream()
                .map(profissionalMapper::toDto)
                .collect(Collectors.toList());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
            Paragraph title = new Paragraph("Relatório de Profissionais", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            Font filterFont = FontFactory.getFont(FontFactory.TIMES_ITALIC, 10, BaseColor.DARK_GRAY);
            StringBuilder filterText = new StringBuilder("Filtros: ");
            if (especialidade != null) filterText.append("Especialidade = ").append(especialidade).append(", ");
            if (status != null) filterText.append("Status = ").append(status).append(", ");
            if (filterText.toString().equals("Filtros: ")) filterText.append("Todos");
            else filterText.delete(filterText.length() - 2, filterText.length());

            Paragraph filters = new Paragraph(filterText.toString(), filterFont);
            document.add(filters);
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 3, 2, 2, 1});

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
            BaseColor headerColor = new BaseColor(0, 150, 76);

            addTableHeader(table, headerFont, headerColor, "ID", "Nome", "Especialidade", "Telefone", "Status");

            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

            for (ProfissionalDTO profissional : profissionaisDTO) {
                table.addCell(createCell(profissional.getId().toString(), dataFont, Element.ALIGN_CENTER));
                table.addCell(createCell(profissional.getNome(), dataFont, Element.ALIGN_LEFT));
                table.addCell(createCell(profissional.getEspecialidade() != null ? profissional.getEspecialidade() : "N/A", dataFont, Element.ALIGN_LEFT));
                table.addCell(createCell(profissional.getTelefone() != null ? profissional.getTelefone() : "N/A", dataFont, Element.ALIGN_LEFT));
                table.addCell(createCell(profissional.getStatus().toString(), dataFont, Element.ALIGN_CENTER));
            }

            document.add(table);
            document.add(Chunk.NEWLINE);

            Font footerFont = FontFactory.getFont(FontFactory.TIMES_ITALIC, 8, BaseColor.GRAY);
            Paragraph footer = new Paragraph("Relatório gerado em " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + " - HomeCare Coop", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório PDF de profissionais", e);
        }
    }

    public byte[] gerarRelatorioProfissionaisJSON(String especialidade, String status) {
        List<Profissional> profissionais = buscarProfissionais(especialidade, status);
        List<ProfissionalDTO> profissionaisDTO = profissionais.stream()
                .map(profissionalMapper::toDto)
                .collect(Collectors.toList());

        try {
            return objectMapper.writeValueAsBytes(profissionaisDTO);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório JSON de profissionais", e);
        }
    }

    public byte[] gerarRelatorioProfissionaisCSV(String especialidade, String status) {
        List<Profissional> profissionais = buscarProfissionais(especialidade, status);
        List<ProfissionalDTO> profissionaisDTO = profissionais.stream()
                .map(profissionalMapper::toDto)
                .collect(Collectors.toList());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(osw)) {

            writer.println("ID,Nome,Email,Telefone,CPF,Especialidade,Registro Profissional,Status");

            for (ProfissionalDTO profissional : profissionaisDTO) {
                writer.printf("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s%n",
                        profissional.getId(),
                        escapeCsv(profissional.getNome()),
                        escapeCsv(profissional.getEmail()),
                        escapeCsv(profissional.getTelefone()),
                        escapeCsv(profissional.getCpf()),
                        escapeCsv(profissional.getEspecialidade()),
                        escapeCsv(profissional.getRegistroProfissional()),
                        profissional.getStatus() != null ? profissional.getStatus().toString() : ""
                );
            }

            writer.flush();
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar relatório CSV de profissionais", e);
        }
    }

    public byte[] gerarRelatorioAgendamentosPDF(LocalDate dataInicio, LocalDate dataFim, String status) {
        List<Agendamento> agendamentos = buscarAgendamentos(dataInicio, dataFim, status);
        List<AgendamentoDTO> agendamentosDTO = agendamentos.stream()
                .map(agendamentoMapper::toDto)
                .collect(Collectors.toList());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
            Paragraph title = new Paragraph("Relatório de Agendamentos", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            Font filterFont = FontFactory.getFont(FontFactory.TIMES_ITALIC, 10, BaseColor.DARK_GRAY);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            StringBuilder filterText = new StringBuilder("Filtros: ");
            filterText.append("Período = ").append(dataInicio.format(formatter))
                    .append(" a ").append(dataFim.format(formatter));
            if (status != null) filterText.append(", Status = ").append(status);

            Paragraph filters = new Paragraph(filterText.toString(), filterFont);
            document.add(filters);
            document.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1, 2, 2, 1.5f, 1.5f, 2, 1});

            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
            BaseColor headerColor = new BaseColor(0, 150, 76);

            addTableHeader(table, headerFont, headerColor, "ID", "Paciente", "Profissional", "Data", "Horário", "Tipo", "Status");

            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

            for (AgendamentoDTO agendamento : agendamentosDTO) {
                table.addCell(createCell(agendamento.getId().toString(), dataFont, Element.ALIGN_CENTER));
                table.addCell(createCell(agendamento.getNomePaciente(), dataFont, Element.ALIGN_LEFT));
                table.addCell(createCell(agendamento.getNomeProfissional(), dataFont, Element.ALIGN_LEFT));
                table.addCell(createCell(agendamento.getData().format(formatter), dataFont, Element.ALIGN_CENTER));

                String horario = agendamento.getHoraInicio().toString() + " - " + agendamento.getHoraFim().toString();
                table.addCell(createCell(horario, dataFont, Element.ALIGN_CENTER));

                table.addCell(createCell(agendamento.getTipo(), dataFont, Element.ALIGN_LEFT));
                table.addCell(createCell(agendamento.getStatus().toString(), dataFont, Element.ALIGN_CENTER));
            }

            document.add(table);
            document.add(Chunk.NEWLINE);

            Font footerFont = FontFactory.getFont(FontFactory.TIMES_ITALIC, 8, BaseColor.GRAY);
            Paragraph footer = new Paragraph("Relatório gerado em " + LocalDate.now().format(formatter) + " - HomeCare Coop", footerFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório PDF de agendamentos", e);
        }
    }

    public byte[] gerarRelatorioAgendamentosJSON(LocalDate dataInicio, LocalDate dataFim, String status) {
        List<Agendamento> agendamentos = buscarAgendamentos(dataInicio, dataFim, status);
        List<AgendamentoDTO> agendamentosDTO = agendamentos.stream()
                .map(agendamentoMapper::toDto)
                .collect(Collectors.toList());

        try {
            return objectMapper.writeValueAsBytes(agendamentosDTO);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar relatório JSON de agendamentos", e);
        }
    }

    public byte[] gerarRelatorioAgendamentosCSV(LocalDate dataInicio, LocalDate dataFim, String status) {
        List<Agendamento> agendamentos = buscarAgendamentos(dataInicio, dataFim, status);
        List<AgendamentoDTO> agendamentosDTO = agendamentos.stream()
                .map(agendamentoMapper::toDto)
                .collect(Collectors.toList());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(osw)) {

            writer.println("ID,Paciente,Profissional,Data,Horário Início,Horário Fim,Tipo,Status,Modalidade,Especialidade,Observações");

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (AgendamentoDTO agendamento : agendamentosDTO) {
                writer.printf("%d,\"%s\",\"%s\",%s,%s,%s,\"%s\",%s,\"%s\",\"%s\",\"%s\"%n",
                        agendamento.getId(),
                        escapeCsv(agendamento.getNomePaciente()),
                        escapeCsv(agendamento.getNomeProfissional()),
                        agendamento.getData().format(dateFormatter),
                        agendamento.getHoraInicio() != null ? agendamento.getHoraInicio().toString() : "",
                        agendamento.getHoraFim() != null ? agendamento.getHoraFim().toString() : "",
                        escapeCsv(agendamento.getTipo()),
                        agendamento.getStatus() != null ? agendamento.getStatus().toString() : "",
                        escapeCsv(agendamento.getModalidade()),
                        escapeCsv(agendamento.getEspecialidade()),
                        escapeCsv(agendamento.getObservacoes())
                );
            }

            writer.flush();
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar relatório CSV de agendamentos", e);
        }
    }

    public byte[] gerarRelatorioEscalasCSV(LocalDate dataInicio, LocalDate dataFim, Long profissionalId, String especialidade, String status) {
        List<Escala> escalas = buscarEscalas(dataInicio, dataFim, profissionalId, especialidade, status);

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(baos, StandardCharsets.UTF_8);
             PrintWriter writer = new PrintWriter(osw)) {

            writer.println("ID,Profissional,Paciente,Data,Horário Início,Horário Fim,Status,Observações");

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            for (Escala escala : escalas) {
                 String nomeProfissional = escala.getProfissional() != null ? escapeCsv(escala.getProfissional().getNome()) : "";
                 String nomePaciente = escala.getPaciente() != null ? escapeCsv(escala.getPaciente().getNome()) : "";

                writer.printf("%d,\"%s\",\"%s\",%s,%s,%s,%s,\"%s\"%n",
                        escala.getId(),
                        nomeProfissional,
                        nomePaciente,
                        escala.getData().format(dateFormatter),
                        escala.getHoraInicio() != null ? escala.getHoraInicio().toString() : "",
                        escala.getHoraFim() != null ? escala.getHoraFim().toString() : "",
                        escala.getStatus() != null ? escala.getStatus().toString() : "",
                        escapeCsv(escala.getObservacoes())
                );
            }

            writer.flush();
            return baos.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException("Erro ao gerar relatório CSV de escalas", e);
        }
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        String escaped = value.replace("\n", " ").replace("\r", " ").replace("\"", "\"\"");
        if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n") || escaped.contains("\r")) {
             return "\"" + escaped + "\"";
        }
        return escaped;
    }

    private List<Paciente> buscarPacientes(String status) {
        Specification<Paciente> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(status)) {
                try {
                    Paciente.StatusPaciente statusEnum = Paciente.StatusPaciente.valueOf(status.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException ignored) {
                    
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return pacienteRepository.findAll(spec);
    }

    private List<Profissional> buscarProfissionais(String especialidade, String status) {
        Specification<Profissional> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(especialidade)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("especialidade")),
                        "%" + especialidade.toLowerCase() + "%"
                ));
            }

            if (StringUtils.hasText(status)) {
                try {
                    Profissional.StatusProfissional statusEnum = Profissional.StatusProfissional.valueOf(status.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException ignored) {
                    // Ignora status inválido
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return profissionalRepository.findAll(spec);
    }

    private List<Agendamento> buscarAgendamentos(LocalDate dataInicio, LocalDate dataFim, String status) {
        Specification<Agendamento> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("data"), dataInicio));
            predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("data"), dataFim));

            if (StringUtils.hasText(status)) {
                try {
                    Agendamento.StatusAgendamento statusEnum = Agendamento.StatusAgendamento.valueOf(status.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("status"), statusEnum));
                } catch (IllegalArgumentException ignored) {
                    // Ignora status inválido
                }
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return agendamentoRepository.findAll(spec);
    }

    private void addTableHeader(PdfPTable table, Font font, BaseColor color, String... headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, font));
            cell.setBackgroundColor(color);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(5);
            table.addCell(cell);
        }
    }

    private PdfPCell createCell(String text, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        return cell;
    }

    private String calcularIdade(LocalDate dataNascimento) {
        return String.valueOf(LocalDate.now().getYear() - dataNascimento.getYear());
    }

    private List<Escala> buscarEscalas(LocalDate dataInicio, LocalDate dataFim, Long profissionalId, String especialidade, String status) {
        Specification<Escala> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (dataInicio != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("data"), dataInicio));
            }
            if (dataFim != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("data"), dataFim));
            }
            if (profissionalId != null) {
                 predicates.add(criteriaBuilder.equal(root.get("profissional").get("id"), profissionalId));
            }
             if (StringUtils.hasText(especialidade)) {
                predicates.add(criteriaBuilder.like(
                         criteriaBuilder.lower(root.get("profissional").get("especialidade")),
                         "%" + especialidade.toLowerCase() + "%"
                 ));
             }
             if (StringUtils.hasText(status)) {
                 try {
                     Escala.StatusEscala statusEnum = Escala.StatusEscala.valueOf(status.toUpperCase());
                     predicates.add(criteriaBuilder.equal(root.get("status"), statusEnum));
                 } catch (IllegalArgumentException ignored) {
                     // Ignora status inválido
                 }
             }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return escalaRepository.findAll(spec);
    }

    public byte[] gerarRelatorioEscalasPDF(LocalDate dataInicio, LocalDate dataFim, Long profissionalId, String especialidade, String status) {
         List<Escala> escalas = buscarEscalas(dataInicio, dataFim, profissionalId, especialidade, status);

         try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
             Document document = new Document(PageSize.A4.rotate()); // Paisagem
             PdfWriter.getInstance(document, baos);
             document.open();

             // Título
             Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, BaseColor.BLACK);
             Paragraph title = new Paragraph("Relatório de Escalas", titleFont);
             title.setAlignment(Element.ALIGN_CENTER);
             document.add(title);
             document.add(Chunk.NEWLINE);

             // Filtros aplicados
             Font filterFont = FontFactory.getFont(FontFactory.TIMES_ITALIC, 10, BaseColor.DARK_GRAY);
             DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
             StringBuilder filterText = new StringBuilder("Filtros: ");
             filterText.append("Período = ").append(dataInicio != null ? dataInicio.format(formatter) : "Início")
                     .append(" a ").append(dataFim != null ? dataFim.format(formatter) : "Fim");
             if (profissionalId != null) filterText.append(", Profissional ID = ").append(profissionalId);
             if (StringUtils.hasText(especialidade)) filterText.append(", Especialidade = ").append(especialidade);
             if (StringUtils.hasText(status)) filterText.append(", Status = ").append(status);

             Paragraph filters = new Paragraph(filterText.toString(), filterFont);
             document.add(filters);
             document.add(Chunk.NEWLINE);

             // Tabela
             PdfPTable table = new PdfPTable(7);
             table.setWidthPercentage(100);
             table.setWidths(new float[]{1, 2, 2, 1.5f, 1.5f, 1.5f, 1});

             // Cabeçalho da tabela
             Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
             BaseColor headerColor = new BaseColor(0, 150, 76); // Verde

             addTableHeader(table, headerFont, headerColor, "ID", "Profissional", "Paciente", "Data", "Horário", "Status", "Observações");

             // Dados da tabela
             Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);

             for (Escala escala : escalas) {
                 table.addCell(createCell(escala.getId().toString(), dataFont, Element.ALIGN_CENTER));
                 String nomeProfissional = escala.getProfissional() != null ? escala.getProfissional().getNome() : "N/A";
                 table.addCell(createCell(nomeProfissional, dataFont, Element.ALIGN_LEFT));
                  String nomePaciente = escala.getPaciente() != null ? escala.getPaciente().getNome() : "N/A";
                 table.addCell(createCell(nomePaciente, dataFont, Element.ALIGN_LEFT));
                 table.addCell(createCell(escala.getData().format(formatter), dataFont, Element.ALIGN_CENTER));
                 String horario = escala.getHoraInicio().toString() + " - " + escala.getHoraFim().toString();
                 table.addCell(createCell(horario, dataFont, Element.ALIGN_CENTER));
                 table.addCell(createCell(escala.getStatus().toString(), dataFont, Element.ALIGN_CENTER));
                 table.addCell(createCell(escala.getObservacoes() != null ? escala.getObservacoes() : "", dataFont, Element.ALIGN_LEFT));
             }

             document.add(table);
             document.add(Chunk.NEWLINE);

             // Rodapé
             Font footerFont = FontFactory.getFont(FontFactory.TIMES_ITALIC, 8, BaseColor.GRAY);
             Paragraph footer = new Paragraph("Relatório gerado em " + LocalDate.now().format(formatter) + " - HomeCare Coop", footerFont);
             footer.setAlignment(Element.ALIGN_CENTER);
             document.add(footer);

             document.close();
             return baos.toByteArray();
         } catch (Exception e) {
             throw new RuntimeException("Erro ao gerar relatório PDF de escalas", e);
         }
     }

     public byte[] gerarRelatorioEscalasJSON(LocalDate dataInicio, LocalDate dataFim, Long profissionalId, String especialidade, String status) {
         List<Escala> escalas = buscarEscalas(dataInicio, dataFim, profissionalId, especialidade, status);
         List<EscalaDTO> escalasDTO = escalas.stream()
                 .map(escalaMapper::toDto)
                 .collect(Collectors.toList());

         try {
             return objectMapper.writeValueAsBytes(escalasDTO);
         } catch (Exception e) {
             throw new RuntimeException("Erro ao gerar relatório JSON de escalas", e);
         }
     }
}
