package com.rafaela.estoque.api;

import com.rafaela.estoque.domain.AlocacaoJaLiberadaException;
import com.rafaela.estoque.domain.EstoqueInsuficienteException;
import com.rafaela.estoque.domain.QuantidadeInvalidaException;
import com.rafaela.estoque.domain.RecursoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@RestControllerAdvice
public class TratamentoDeErros extends ResponseEntityExceptionHandler {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ProblemDetail naoEncontrado(RecursoNaoEncontradoException ex) {
        return problema(HttpStatus.NOT_FOUND, "Recurso não encontrado", ex);
    }

    @ExceptionHandler(EstoqueInsuficienteException.class)
    public ProblemDetail estoqueInsuficiente(EstoqueInsuficienteException ex) {
        return problema(HttpStatus.UNPROCESSABLE_CONTENT, "Estoque insuficiente", ex);
    }

    @ExceptionHandler(AlocacaoJaLiberadaException.class)
    public ProblemDetail jaLiberada(AlocacaoJaLiberadaException ex) {
        return problema(HttpStatus.CONFLICT, "Alocação já liberada", ex);
    }

    @ExceptionHandler(QuantidadeInvalidaException.class)
    public ProblemDetail quantidadeInvalida(QuantidadeInvalidaException ex) {
        return problema(HttpStatus.BAD_REQUEST, "Quantidade inválida", ex);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail conflitoDeConcorrencia(ObjectOptimisticLockingFailureException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "O produto foi alterado por outra operação ao mesmo tempo. Tente novamente.");
        problema.setTitle("Conflito de concorrência");
        return problema;
    }

    private ProblemDetail problema(HttpStatus status, String titulo, RuntimeException ex) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(status, ex.getMessage());
        problema.setTitle(titulo);
        return problema;
    }
}