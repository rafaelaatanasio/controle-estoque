package com.rafaela.estoque.repository;

import com.rafaela.estoque.domain.Produto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProdutoRepository extends JpaRepository<Produto, String> {

    // SELECT ... FOR UPDATE: trava a linha do produto até o fim da transação
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Produto p where p.id = :id")
    Optional<Produto> buscarParaAtualizacao(@Param("id") String id);
}

// 2. extends JpaRepository<Produto, String>
//Herda prontos dezenas de métodos para conversar com o banco, por exemplo:
// save(produto) → salva ou atualiza
// findById("SKU-1") → busca pelo id
// findAll() → lista todos
// deleteById(...) → apaga
// Sem escrever nenhuma linha de SQL.
//
// 3. O que vai dentro de < > (nome técnico: generics)
// Duas informações: qual entidade e qual o tipo do id dela.
//
// Repository	Entidade	Tipo do id
// ProdutoRepository	Produto	String (“SKU-1”)
// ReservaRepository	Reserva	UUID
// BloqueioRepository	Bloqueio	UUID