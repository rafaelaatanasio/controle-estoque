package com.rafaela.estoque.api.dto;

import com.rafaela.estoque.domain.Produto;

public record ProdutoResponse(
        String id,
        int quantidadeTotal,
        int quantidadeDisponivel,
        int quantidadeReservada,
        int quantidadeBloqueada
) {
    public static ProdutoResponse from(Produto produto) {
        return new ProdutoResponse(
                produto.getId(),
                produto.getQuantidadeTotal(),
                produto.getQuantidadeDisponivel(),
                produto.getQuantidadeReservada(),
                produto.getQuantidadeBloqueada());
    }
}