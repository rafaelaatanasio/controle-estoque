package com.rafaela.estoque.domain;

public class RecursoNaoEncontradoException extends RuntimeException {

    public RecursoNaoEncontradoException(String tipo, Object id) {
        super("%s %s não encontrado(a)".formatted(tipo, id));
    }
}