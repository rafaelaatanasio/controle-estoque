package com.rafaela.estoque.repository;

import com.rafaela.estoque.domain.Bloqueio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BloqueioRepository extends JpaRepository<Bloqueio, UUID> {
}