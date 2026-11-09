package com.rafaela.estoque.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.time.Instant;
import java.util.UUID;

@Entity
public class Bloqueio {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String produtoId;
    private String motivo;
    private int quantidade;

    @Enumerated(EnumType.STRING)
    private StatusAlocacao status;

    private Instant criadoEm;
    private Instant liberadoEm;

    protected Bloqueio() {
        // construtor exigido pelo JPA
    }

    public Bloqueio(String produtoId, String motivo, int quantidade) {
        this.produtoId = produtoId;
        this.motivo = motivo;
        this.quantidade = quantidade;
        this.status = StatusAlocacao.ATIVA;
        this.criadoEm = Instant.now();
    }

    // 4. Método liberar()
    //
    //Se já está LIBERADA → lança AlocacaoJaLiberadaException (impede liberar duas vezes).
    //Senão → muda para LIBERADA e registra o momento.
    // Para comparar enums usa-se ==, porque cada valor (ATIVA, LIBERADA) existe uma única vez na memória.

    public void liberar() {
        if (status == StatusAlocacao.LIBERADA) {
            throw new AlocacaoJaLiberadaException("Bloqueio", id);
        }
        status = StatusAlocacao.LIBERADA;
        liberadoEm = Instant.now();
    }

    public UUID getId() { return id; }
    public String getProdutoId() { return produtoId; }
    public String getMotivo() { return motivo; }
    public int getQuantidade() { return quantidade; }
    public StatusAlocacao getStatus() { return status; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getLiberadoEm() { return liberadoEm; }
}