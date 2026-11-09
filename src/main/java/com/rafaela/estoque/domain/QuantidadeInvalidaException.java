package com.rafaela.estoque.domain;
// exceções personalizadas, meu tipo de erro
public class QuantidadeInvalidaException extends RuntimeException {
// extends Herança. Minha classe tá herdando tudo de RuntimeException, uma exceção que já existe no Java
    // não checada, não preciso de try/catch, mais clean
    public QuantidadeInvalidaException(int quantidade) {
        super("A quantidade deve ser maior que zero. Recebido: " + quantidade);
    }
}