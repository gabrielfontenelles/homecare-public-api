package com.example.homecare_adm_app.api.repository;

import com.example.homecare_adm_app.api.model.Agendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AgendamentoRepository extends JpaRepository<Agendamento, Long>, JpaSpecificationExecutor<Agendamento> {

    List<Agendamento> findByData(LocalDate data);

    long countByData(LocalDate data);

    List<Agendamento> findByPacienteId(Long pacienteId);

    List<Agendamento> findByProfissionalId(Long profissionalId);

    List<Agendamento> findByPacienteIdAndData(Long pacienteId, LocalDate data);

    List<Agendamento> findByProfissionalIdAndData(Long profissionalId, LocalDate data);

    List<Agendamento> findByPacienteIdAndDataBetween(Long pacienteId, LocalDate dataInicio, LocalDate dataFim);

    List<Agendamento> findByProfissionalIdAndDataBetween(Long profissionalId, LocalDate dataInicio, LocalDate dataFim);

    Optional<Agendamento> findFirstByPacienteIdOrderByDataDesc(Long pacienteId);

    long count();

    @Query("SELECT a FROM Agendamento a WHERE a.profissional.id = :profissionalId " +
           "AND a.data = :data " +
           "AND ((a.horaInicio < :horaFim AND a.horaFim > :horaInicio) " +
           "OR (a.horaInicio >= :horaInicio AND a.horaInicio < :horaFim) " +
           "OR (a.horaFim > :horaInicio AND a.horaFim <= :horaFim)) " +
           "AND (:agendamentoId is null OR a.id <> :agendamentoId)")
    List<Agendamento> findSobreposicoesProfissional(
            @Param("profissionalId") Long profissionalId,
            @Param("data") LocalDate data,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFim") LocalTime horaFim,
            @Param("agendamentoId") Long agendamentoId);

    @Query("SELECT a FROM Agendamento a WHERE a.paciente.id = :pacienteId " +
           "AND a.data = :data " +
           "AND ((a.horaInicio < :horaFim AND a.horaFim > :horaInicio) " +
           "OR (a.horaInicio >= :horaInicio AND a.horaInicio < :horaFim) " +
           "OR (a.horaFim > :horaInicio AND a.horaFim <= :horaFim)) " +
           "AND (:agendamentoId is null OR a.id <> :agendamentoId)")
    List<Agendamento> findSobreposicoesPaciente(
            @Param("pacienteId") Long pacienteId,
            @Param("data") LocalDate data,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFim") LocalTime horaFim,
            @Param("agendamentoId") Long agendamentoId);

    @Query("SELECT a FROM Agendamento a WHERE (a.status = 'PENDENTE' OR a.status = 'CONFIRMADO') AND (a.data < :hoje OR (a.data = :hoje AND a.horaFim < :agora))")
    List<Agendamento> findPendentesOuConfirmadosAte(@Param("hoje") java.time.LocalDate hoje, @Param("agora") java.time.LocalTime agora);
}
