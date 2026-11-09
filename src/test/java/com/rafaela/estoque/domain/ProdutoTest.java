package com.rafaela.estoque.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProdutoTest {

    private Produto produto;

    @BeforeEach
    void setUp() {
        produto = new Produto("SKU-1");
        produto.registrarEntrada(10);
    }

    @Test
    void entradaAumentaTotalEDisponivel() {
        produto.registrarEntrada(5);
        assertEstado(15, 15, 0, 0);
    }

    @Test
    void saidaReduzTotalEDisponivel() {
        produto.registrarSaida(4);
        assertEstado(6, 6, 0, 0);
    }

    @Test
    void saidaMaiorQueDisponivelFalhaENaoAlteraEstoque() {

        assertThatThrownBy(() -> produto.registrarSaida(11))
                .isInstanceOf(EstoqueInsuficienteException.class);
        assertEstado(10, 10, 0, 0);
    }

    @Test
    void saidaNaoPodeUsarUnidadesReservadas() {
        produto.reservar(8);
        assertThatThrownBy(() -> produto.registrarSaida(5))
                .isInstanceOf(EstoqueInsuficienteException.class);
        assertEstado(10, 2, 8, 0);
    }

    @Test
    void reservaMoveDoDisponivelParaReservadoSemAlterarTotal() {
        produto.reservar(3);
        assertEstado(10, 7, 3, 0);
    }

    @Test
    void naoPermiteReservarMaisQueODisponivel() {
        assertThatThrownBy(() -> produto.reservar(11))
                .isInstanceOf(EstoqueInsuficienteException.class);
        assertEstado(10, 10, 0, 0);
    }

    @Test
    void bloqueioConsideraUnidadesJaReservadas() {
        produto.reservar(6);
        assertThatThrownBy(() -> produto.bloquear(5))
                .isInstanceOf(EstoqueInsuficienteException.class);
        assertEstado(10, 4, 6, 0);
    }

    @Test
    void liberarReservaDevolveAoDisponivel() {
        produto.reservar(4);
        produto.liberarReserva(4);
        assertEstado(10, 10, 0, 0);
    }

    @Test
    void liberarBloqueioDevolveAoDisponivel() {
        produto.bloquear(3);
        produto.liberarBloqueio(3);
        assertEstado(10, 10, 0, 0);
    }

    @Test
    void naoPermiteLiberarMaisQueOReservado() {
        produto.reservar(2);
        assertThatThrownBy(() -> produto.liberarReserva(3))
                .isInstanceOf(IllegalStateException.class);
        assertEstado(10, 8, 2, 0);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, -100})
    void rejeitaQuantidadeZeroOuNegativa(int quantidade) {
        assertThatThrownBy(() -> produto.registrarEntrada(quantidade)).isInstanceOf(QuantidadeInvalidaException.class);
        assertThatThrownBy(() -> produto.registrarSaida(quantidade)).isInstanceOf(QuantidadeInvalidaException.class);
        assertThatThrownBy(() -> produto.reservar(quantidade)).isInstanceOf(QuantidadeInvalidaException.class);
        assertThatThrownBy(() -> produto.bloquear(quantidade)).isInstanceOf(QuantidadeInvalidaException.class);
        assertEstado(10, 10, 0, 0);
    }

    @Test
    void invarianteSeMantemAposVariasOperacoes() {
        produto.reservar(3);
        produto.bloquear(2);
        produto.registrarSaida(4);
        produto.liberarReserva(3);
        produto.registrarEntrada(7);
        assertEstado(13, 11, 0, 2);
    }

    private void assertEstado(int total, int disponivel, int reservado, int bloqueado) {
        assertThat(produto.getQuantidadeTotal()).as("total").isEqualTo(total);
        assertThat(produto.getQuantidadeDisponivel()).as("disponível").isEqualTo(disponivel);
        assertThat(produto.getQuantidadeReservada()).as("reservado").isEqualTo(reservado);
        assertThat(produto.getQuantidadeBloqueada()).as("bloqueado").isEqualTo(bloqueado);
        // invariante: total = disponível + reservado + bloqueado
        assertThat(produto.getQuantidadeTotal()).isEqualTo(
                produto.getQuantidadeDisponivel() + produto.getQuantidadeReservada() + produto.getQuantidadeBloqueada());
    }
}