package com.rafaela.estoque.service;

import com.rafaela.estoque.domain.AlocacaoJaLiberadaException;
import com.rafaela.estoque.domain.Bloqueio;
import com.rafaela.estoque.domain.EstoqueInsuficienteException;
import com.rafaela.estoque.domain.Produto;
import com.rafaela.estoque.domain.RecursoNaoEncontradoException;
import com.rafaela.estoque.domain.Reserva;
import com.rafaela.estoque.domain.StatusAlocacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class EstoqueServiceTest {

    private static final String SKU = "SKU-1";

    @Autowired
    private EstoqueService service;

    @BeforeEach
    void setUp() {
        service.registrarEntrada(SKU, 10);
    }

    @Test
    void reservarCriaReservaAtivaComIdentificacao() {
        Reserva reserva = service.reservar(SKU, "PEDIDO-1", 4);

        assertThat(reserva.getId()).isNotNull();
        assertThat(reserva.getStatus()).isEqualTo(StatusAlocacao.ATIVA);
        assertThat(service.consultarReserva(reserva.getId()).getSolicitacaoId()).isEqualTo("PEDIDO-1");
        assertEstado(10, 6, 4, 0);
    }

    @Test
    void produtoPodeTerVariasReservasEBloqueiosAoMesmoTempo() {
        Reserva pedido1 = service.reservar(SKU, "PEDIDO-1", 2);
        service.reservar(SKU, "PEDIDO-2", 3);
        service.bloquear(SKU, "AVARIA", 1);
        assertEstado(10, 4, 5, 1);

        service.liberarReserva(pedido1.getId());

        assertEstado(10, 6, 3, 1);
    }

    @Test
    void liberarReservaDevolveQuantidadeAoDisponivel() {
        Reserva reserva = service.reservar(SKU, "PEDIDO-1", 4);

        Reserva liberada = service.liberarReserva(reserva.getId());

        assertThat(liberada.getStatus()).isEqualTo(StatusAlocacao.LIBERADA);
        assertThat(liberada.getLiberadaEm()).isNotNull();
        assertEstado(10, 10, 0, 0);
    }

    @Test
    void liberarBloqueioDevolveQuantidadeAoDisponivel() {
        Bloqueio bloqueio = service.bloquear(SKU, "INVENTARIO", 3);

        service.liberarBloqueio(bloqueio.getId());

        assertEstado(10, 10, 0, 0);
    }

    @Test
    void liberarReservaDuasVezesFalhaENaoAlteraEstoque() {
        Reserva reserva = service.reservar(SKU, "PEDIDO-1", 4);
        service.liberarReserva(reserva.getId());

        assertThatThrownBy(() -> service.liberarReserva(reserva.getId()))
                .isInstanceOf(AlocacaoJaLiberadaException.class);
        assertEstado(10, 10, 0, 0);
    }

    @Test
    void reservaMaiorQueDisponivelNaoCriaReserva() {
        assertThatThrownBy(() -> service.reservar(SKU, "PEDIDO-1", 11))
                .isInstanceOf(EstoqueInsuficienteException.class);
        assertEstado(10, 10, 0, 0);
    }

    @Test
    void operacaoEmProdutoInexistenteFalha() {
        assertThatThrownBy(() -> service.reservar("NAO-EXISTE", "PEDIDO-1", 1))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void liberarReservaInexistenteFalha() {
        assertThatThrownBy(() -> service.liberarReserva(UUID.randomUUID()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    private void assertEstado(int total, int disponivel, int reservado, int bloqueado) {
        Produto produto = service.consultarProduto(SKU);
        assertThat(produto.getQuantidadeTotal()).as("total").isEqualTo(total);
        assertThat(produto.getQuantidadeDisponivel()).as("disponível").isEqualTo(disponivel);
        assertThat(produto.getQuantidadeReservada()).as("reservado").isEqualTo(reservado);
        assertThat(produto.getQuantidadeBloqueada()).as("bloqueado").isEqualTo(bloqueado);
    }
}