package com.rafaela.estoque.domain;
// JUnit → o “motor” que roda os testes (@Test, @BeforeEach...).
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
// AssertJ → as frases de verificação (assertThat, assertThatThrownBy).
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
// import static → permite escrever assertThat(...) direto, sem o nome da classe na frente.
class ProdutoTest {

    private Produto produto;

    // Roda antes de cada teste: cria um produto novo SKU-1 com 10 unidades.
    // Assim todo teste começa do mesmo ponto e um não interfere no outro.
    @BeforeEach
    void setUp() {
        produto = new Produto("SKU-1");
        produto.registrarEntrada(10);
    }

    // Marca um método como teste. O nome já descreve o que se espera,
    //  ex.: saidaReduzTotalEDisponivel. Isso é boa prática e ajuda a ler o resultado.
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
        // 5. Testando erros
        //assertThatThrownBy(() -> produto.registrarSaida(11)).isInstanceOf(...)
        //
        //() -> é uma lambda: um “pedaço de código guardado” para o teste executar e capturar o erro.
        //isInstanceOf → confere se o erro é do tipo esperado.
        assertThatThrownBy(() -> produto.registrarSaida(11))
                .isInstanceOf(EstoqueInsuficienteException.class);
        assertEstado(10, 10, 0, 0);
    }

    // 4. O padrão de cada teste: Preparar → Agir → Verificar 🟥
    //Ex.: saidaNaoPodeUsarUnidadesReservadas
    //
    //Preparar: reserva 8 (sobram 2 disponíveis).
    //Agir: tenta tirar 5.
    //Verificar: espera EstoqueInsuficienteException e confere que nada mudou.
    //
    //(Nome técnico: AAA, Arrange, Act, Assert.)
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

    // 6. @ParameterizedTest + @ValueSource
    //Roda o mesmo teste várias vezes com valores diferentes (0, -1, -100). Evita escrever três testes iguais.
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

    // 7. assertEstado(...) — o ajudante
    // Método private que confere os 4 números de uma vez
    // (total, disponível, reservado, bloqueado). E ainda checa a invariante:
    // total = disponível + reservado + bloqueado
    // Invariante = uma regra que precisa ser verdade sempre, depois de qualquer operação. É a “lei” do seu estoque.

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

// 8. invarianteSeMantemAposVariasOperacoes
//Simula várias operações seguidas para garantir que a conta nunca quebra. Começa com 10:
//reserva 3 → bloqueia 2 → saída 4 → libera 3 → entrada 7 → total 13, disponível 11, reservado 0, bloqueado 2.