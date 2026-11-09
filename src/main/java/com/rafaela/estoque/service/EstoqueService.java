package com.rafaela.estoque.service;

import com.rafaela.estoque.domain.Bloqueio;
import com.rafaela.estoque.domain.Produto;
import com.rafaela.estoque.domain.RecursoNaoEncontradoException;
import com.rafaela.estoque.domain.Reserva;
import com.rafaela.estoque.repository.BloqueioRepository;
import com.rafaela.estoque.repository.ProdutoRepository;
import com.rafaela.estoque.repository.ReservaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class EstoqueService {

    private final ProdutoRepository produtoRepository;
    private final ReservaRepository reservaRepository;
    private final BloqueioRepository bloqueioRepository;

    public EstoqueService(ProdutoRepository produtoRepository,
                          ReservaRepository reservaRepository,
                          BloqueioRepository bloqueioRepository) {
        this.produtoRepository = produtoRepository;
        this.reservaRepository = reservaRepository;
        this.bloqueioRepository = bloqueioRepository;
    }

    @Transactional
    public Produto registrarEntrada(String produtoId, int quantidade) {
        Produto produto = produtoRepository.findById(produtoId)
                .orElseGet(() -> new Produto(produtoId));
        produto.registrarEntrada(quantidade);
        return produtoRepository.save(produto);
    }

    @Transactional
    public Produto registrarSaida(String produtoId, int quantidade) {
        Produto produto = buscarProduto(produtoId);
        produto.registrarSaida(quantidade);
        return produto;
    }

    @Transactional
    public Reserva reservar(String produtoId, String solicitacaoId, int quantidade) {
        Produto produto = buscarProduto(produtoId);
        produto.reservar(quantidade);
        return reservaRepository.save(new Reserva(produtoId, solicitacaoId, quantidade));
    }

    @Transactional
    public Bloqueio bloquear(String produtoId, String motivo, int quantidade) {
        Produto produto = buscarProduto(produtoId);
        produto.bloquear(quantidade);
        return bloqueioRepository.save(new Bloqueio(produtoId, motivo, quantidade));
    }

    @Transactional
    public Reserva liberarReserva(UUID reservaId) {
        Reserva reserva = consultarReserva(reservaId);
        reserva.liberar();
        buscarProduto(reserva.getProdutoId()).liberarReserva(reserva.getQuantidade());
        return reserva;
    }

    @Transactional
    public Bloqueio liberarBloqueio(UUID bloqueioId) {
        Bloqueio bloqueio = consultarBloqueio(bloqueioId);
        bloqueio.liberar();
        buscarProduto(bloqueio.getProdutoId()).liberarBloqueio(bloqueio.getQuantidade());
        return bloqueio;
    }

    @Transactional(readOnly = true)
    public Produto consultarProduto(String produtoId) {
        return buscarProduto(produtoId);
    }

    @Transactional(readOnly = true)
    public Reserva consultarReserva(UUID reservaId) {
        return reservaRepository.findById(reservaId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Reserva", reservaId));
    }

    @Transactional(readOnly = true)
    public Bloqueio consultarBloqueio(UUID bloqueioId) {
        return bloqueioRepository.findById(bloqueioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Bloqueio", bloqueioId));
    }

    private Produto buscarProduto(String produtoId) {
        return produtoRepository.findById(produtoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Produto", produtoId));
    }
}