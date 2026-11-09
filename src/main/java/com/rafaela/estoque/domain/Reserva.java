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
public class Reserva {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String produtoId;
    private String solicitacaoId;
    private int quantidade;

    @Enumerated(EnumType.STRING)
    private StatusAlocacao status;

    private Instant criadaEm;
    private Instant liberadaEm;

    protected Reserva() {
        // construtor exigido pelo JPA
    }

    public Reserva(String produtoId, String solicitacaoId, int quantidade) {
        this.produtoId = produtoId;
        this.solicitacaoId = solicitacaoId;
        this.quantidade = quantidade;
        this.status = StatusAlocacao.ATIVA;
        this.criadaEm = Instant.now();
    }

    public void liberar() {
        if (status == StatusAlocacao.LIBERADA) {
            throw new AlocacaoJaLiberadaException("Reserva", id);
        }
        status = StatusAlocacao.LIBERADA;
        liberadaEm = Instant.now();
    }

    public UUID getId() { return id; }
    public String getProdutoId() { return produtoId; }
    public String getSolicitacaoId() { return solicitacaoId; }
    public int getQuantidade() { return quantidade; }
    public StatusAlocacao getStatus() { return status; }
    public Instant getCriadaEm() { return criadaEm; }
    public Instant getLiberadaEm() { return liberadaEm; }
}