package com.rafaela.estoque.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class Produto {

    @Id
    private String id;

    private int quantidadeTotal;
    private int quantidadeReservada;
    private int quantidadeBloqueada;

    @Version

    private Long versao;

    protected Produto() {
    }

    public Produto(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("A identificação do produto é obrigatória");
        }
        this.id = id;
    }

    public int getQuantidadeDisponivel() {
        return quantidadeTotal - quantidadeReservada - quantidadeBloqueada;
    }

    public void registrarEntrada(int quantidade) {
        validarQuantidade(quantidade);

        quantidadeTotal = Math.addExact(quantidadeTotal, quantidade);
    }

    public void registrarSaida(int quantidade) {
        validarQuantidade(quantidade);
        garantirDisponivel(quantidade);
        quantidadeTotal -= quantidade;
    }

    public void reservar(int quantidade) {
        validarQuantidade(quantidade);
        garantirDisponivel(quantidade);
        quantidadeReservada += quantidade;
    }

    public void liberarReserva(int quantidade) {
        validarQuantidade(quantidade);
        if (quantidade > quantidadeReservada) {
            throw new IllegalStateException("Liberação maior que a quantidade reservada do produto " + id);
        }
        quantidadeReservada -= quantidade;
    }

    public void bloquear(int quantidade) {
        validarQuantidade(quantidade);
        garantirDisponivel(quantidade);
        quantidadeBloqueada += quantidade;
    }

    public void liberarBloqueio(int quantidade) {
        validarQuantidade(quantidade);
        if (quantidade > quantidadeBloqueada) {
            throw new IllegalStateException("Liberação maior que a quantidade bloqueada do produto " + id);
        }
        quantidadeBloqueada -= quantidade;
    }

    private static void validarQuantidade(int quantidade) {
        if (quantidade <= 0) {
            throw new QuantidadeInvalidaException(quantidade);
        }
    }

    private void garantirDisponivel(int quantidade) {
        int disponivel = getQuantidadeDisponivel();
        if (quantidade > disponivel) {
            throw new EstoqueInsuficienteException(id, quantidade, disponivel);
        }
    }

    public String getId() { return id; }
    public int getQuantidadeTotal() { return quantidadeTotal; }
    public int getQuantidadeReservada() { return quantidadeReservada; }
    public int getQuantidadeBloqueada() { return quantidadeBloqueada; }
}