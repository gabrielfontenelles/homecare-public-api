package com.example.homecare_adm_app.api.repository;


import com.example.homecare_adm_app.api.model.Escala;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EscalaRepository extends JpaRepository<Escala, Long>, JpaSpecificationExecutor<Escala> {

    List<Escala> findByData(LocalDate data);

    List<Escala> findByProfissionalId(Long profissionalId);

    List<Escala> findByProfissionalIdAndData(Long profissionalId, LocalDate data);

    List<Escala> findByProfissionalIdAndDataBetween(Long profissionalId, LocalDate dataInicio, LocalDate dataFim);

    List<Escala> findByDataBetween(LocalDate dataInicio, LocalDate dataFim);

    List<Escala> findByProfissionalIdAndDataAfter(Long profissionalId, LocalDate data);
}