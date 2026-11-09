package com.rafaela.estoque.domain;

public class QuantidadeInvalidaException extends RuntimeException {

    public QuantidadeInvalidaException(int quantidade) {
        super("A quantidade deve ser maior que zero. Recebido: " + quantidade);
    }
}