package com.rafaela.estoque.repository;

import com.rafaela.estoque.domain.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ReservaRepository extends JpaRepository<Reserva, UUID> {
}
// interface: Lembra do post-it? Interface é um contrato: diz o que fazer, não como.
// Aqui você nem escreve o “como”: o Spring cria a implementação sozinho quando o projeto sobe.