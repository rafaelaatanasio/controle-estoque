package com.rafaela.estoque.domain;
// exceções personalizadas, meu tipo de erro
public class EstoqueInsuficienteException extends RuntimeException {
    // extends Herança. Minha classe tá herdando tudo de RuntimeException, uma exceção que já existe no Java
    // não checada, não preciso de try/catch, mais clean
    public EstoqueInsuficienteException(String produtoId, int solicitada, int disponivel) {
        super("Estoque insuficiente para o produto %s: solicitado %d, disponível %d"
    // O super chama o construtor da RuntimeException, passando a mensagem montada com os parâmetros recebidos
                .formatted(produtoId, solicitada, disponivel));
    }
}

// 2. O construtor
//Recebe as informações do erro como parâmetros:
//
//EstoqueInsuficiente: qual produto, quanto pediram, quanto tinha.
//QuantidadeInvalida: o número inválido que chegou.

// 3. super(...)
//Chama o CONSTRUTOR da classe pai (RuntimeException) passando a mensagem.
// É como dizer: “pai, guarda essa mensagem de erro para mim”.

// 4. Montando a mensagem, duas formas de fazer o mesmo:
//
//"...%s...%d...".formatted(...) → os % são espaços reservados:
// %s = texto (String)
// %d = número inteiro.
// São preenchidos na ordem.
//"... Recebido: " + quantidade → concatenação: junta texto com o valor.