package com.example.homecare_adm_app.api.repository;


import com.example.homecare_adm_app.api.model.Profissional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfissionalRepository extends JpaRepository<Profissional, Long>, JpaSpecificationExecutor<Profissional> {

    long countByStatus(Profissional.StatusProfissional status);

    List<Profissional> findByEspecialidadeContainingIgnoreCase(String especialidade);

    List<Profissional> findByNomeContainingIgnoreCase(String nome);

    Optional<Profissional> findByEmail(String email);

    Optional<Profissional> findByUserId(Long userId);

    List<Profissional> findByStatus(Profissional.StatusProfissional status);
}
