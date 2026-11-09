package com.rafaela.estoque.api.dto;

import com.rafaela.estoque.domain.Bloqueio;
import com.rafaela.estoque.domain.StatusAlocacao;

import java.time.Instant;
import java.util.UUID;

public record BloqueioResponse(
        UUID id,
        String produtoId,
        String motivo,
        int quantidade,
        StatusAlocacao status,
        Instant criadoEm,
        Instant liberadoEm
) {
    public static BloqueioResponse from(Bloqueio bloqueio) {
        return new BloqueioResponse(
                bloqueio.getId(),
                bloqueio.getProdutoId(),
                bloqueio.getMotivo(),
                bloqueio.getQuantidade(),
                bloqueio.getStatus(),
                bloqueio.getCriadoEm(),
                bloqueio.getLiberadoEm());
    }
}