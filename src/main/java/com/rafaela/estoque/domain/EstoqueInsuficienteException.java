package com.rafaela.estoque.domain;

public class EstoqueInsuficienteException extends RuntimeException {

    public EstoqueInsuficienteException(String produtoId, int solicitada, int disponivel) {
        super("Estoque insuficiente para o produto %s: solicitado %d, disponível %d"
                .formatted(produtoId, solicitada, disponivel));
    }
}