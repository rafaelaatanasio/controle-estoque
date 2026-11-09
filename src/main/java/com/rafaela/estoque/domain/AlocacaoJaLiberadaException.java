package com.rafaela.estoque.domain;

import java.util.UUID;

public class AlocacaoJaLiberadaException extends RuntimeException {

    public AlocacaoJaLiberadaException(String tipo, UUID id) {
        super("%s %s já foi liberada".formatted(tipo, id));
    }
}