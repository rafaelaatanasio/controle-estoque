package com.rafaela.estoque.domain;
// as biblios que conversam com o bd
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity // classe vira uma tabela do banco
public class Produto {
    // esse atributo é a identificação única (chave primária) de cada produto.
    @Id
    private String id;
    // 3. Atributos 🟨🟩
    //Todos private = encapsulamento. Ninguém de fora altera a quantidade direto, só pelos métodos.
    //
    //Total = tudo que existe.
    //Reservada = separado para um pedido.
    //Bloqueada = indisponível (ex.: avariado).
    private int quantidadeTotal;
    private int quantidadeReservada;
    private int quantidadeBloqueada;

    // um contador que o JPA aumenta a cada alteração. Se duas pessoas mexerem no mesmo produto
    // ao mesmo tempo, a segunda recebe erro em vez de apagar a mudança da primeira. (Nome técnico:
    // optimistic locking. Ótimo ponto para a entrevista.)
    @Version // é a anotação colocada em cima, falando pra usar esse atributo como controle de versoes
    //é um número de versão que impede que duas alterações ao mesmo tempo no mesmo produto se sobrescrevam.
    private Long versao; // versao é o atributo. private Long versao. a anotação dá o superpoder pra esse atributo aqui
    // 4. Construtores
    //
    //protected Produto() = vazio, exigido pelo JPA para montar o objeto quando lê do banco.
    // É protected para você não usá-lo sem querer.
    //public Produto(String id) = o que você usa. Recusa id vazio lançando uma exceção 🟥.
    // this.id = id significa “o atributo id deste objeto recebe o valor que chegou”.
    protected Produto() { // protegido do restante dos códigos
        // construtor exigido pelo JPA
    }

    public Produto(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("A identificação do produto é obrigatória");
        }
        this.id = id;
    }
    // 5. Disponível
    //getQuantidadeDisponivel() calcula na hora: total − reservada − bloqueada.
    // Não é salvo, então nunca fica desatualizado.
    public int getQuantidadeDisponivel() {
        return quantidadeTotal - quantidadeReservada - quantidadeBloqueada;
    }
    // 6. Métodos de ação 🟨
    //Todos seguem o mesmo roteiro:
    //
    //validarQuantidade → a quantidade é maior que zero?
    //garantirDisponivel → tem estoque suficiente?
    //Só então altera o número.
    public void registrarEntrada(int quantidade) { // parametro, valor que o método recebe
        validarQuantidade(quantidade); // método da classe Produto
        // Detalhe: Math.addExact soma, mas dá erro se o número passar do limite do int,
        // em vez de virar um valor negativo estranho.
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

    //7. Métodos private
    // São “ajudantes internos”, usados só dentro da classe, para não repetir código. static significa que
    // o método não depende dos dados do objeto (só olha o número que recebeu).
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
    // 8. Só getters, sem setters
    //Escolha proposital: não existe setQuantidadeTotal(). A quantidade só muda por
    // métodos com regras. Isso é encapsulamento na prática e vale a pena explicar na entrevista.
    public String getId() { return id; }
    public int getQuantidadeTotal() { return quantidadeTotal; }
    public int getQuantidadeReservada() { return quantidadeReservada; }
    public int getQuantidadeBloqueada() { return quantidadeBloqueada; }
}