package com.rafaela.estoque.api.dto;

import com.rafaela.estoque.domain.Reserva;
import com.rafaela.estoque.domain.StatusAlocacao;

import java.time.Instant;
import java.util.UUID;

public record ReservaResponse(
        UUID id,
        String produtoId,
        String solicitacaoId,
        int quantidade,
        StatusAlocacao status,
        Instant criadaEm,
        Instant liberadaEm
) {
    public static ReservaResponse from(Reserva reserva) {
        return new ReservaResponse(
                reserva.getId(),
                reserva.getProdutoId(),
                reserva.getSolicitacaoId(),
                reserva.getQuantidade(),
                reserva.getStatus(),
                reserva.getCriadaEm(),
                reserva.getLiberadaEm());
    }
}