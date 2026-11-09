package com.rafaela.estoque.concorrencia;

import com.rafaela.estoque.domain.EstoqueInsuficienteException;
import com.rafaela.estoque.domain.Produto;
import com.rafaela.estoque.service.EstoqueService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

// Sem @Transactional: cada thread precisa da sua própria transação, como em produção
@SpringBootTest
class ConcorrenciaTest {

    @Autowired
    private EstoqueService service;

    @Test
    void reservasSimultaneasNuncaUltrapassamODisponivel() throws Exception {
        String sku = "SKU-" + UUID.randomUUID();
        service.registrarEntrada(sku, 10);

        int requisicoes = 20;
        ExecutorService executor = Executors.newFixedThreadPool(requisicoes);
        CountDownLatch largada = new CountDownLatch(1);
        AtomicInteger aceitas = new AtomicInteger();
        AtomicInteger recusadas = new AtomicInteger();
        List<Future<?>> tarefas = new ArrayList<>();

        for (int i = 0; i < requisicoes; i++) {
            String pedido = "PEDIDO-" + i;
            tarefas.add(executor.submit(() -> {
                largada.await(); // todas as threads esperam e começam juntas
                try {
                    service.reservar(sku, pedido, 1);
                    aceitas.incrementAndGet();
                } catch (EstoqueInsuficienteException e) {
                    recusadas.incrementAndGet();
                }
                return null;
            }));
        }

        largada.countDown();
        for (Future<?> tarefa : tarefas) {
            tarefa.get(30, TimeUnit.SECONDS);
        }
        executor.shutdown();

        Produto produto = service.consultarProduto(sku);
        assertThat(aceitas.get()).isEqualTo(10);
        assertThat(recusadas.get()).isEqualTo(10);
        assertThat(produto.getQuantidadeReservada()).isEqualTo(10);
        assertThat(produto.getQuantidadeDisponivel()).isZero();
    }
}