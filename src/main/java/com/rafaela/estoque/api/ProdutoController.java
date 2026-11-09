package com.rafaela.estoque.api;

import com.rafaela.estoque.api.dto.BloqueioRequest;
import com.rafaela.estoque.api.dto.BloqueioResponse;
import com.rafaela.estoque.api.dto.ProdutoResponse;
import com.rafaela.estoque.api.dto.QuantidadeRequest;
import com.rafaela.estoque.api.dto.ReservaRequest;
import com.rafaela.estoque.api.dto.ReservaResponse;
import com.rafaela.estoque.domain.Bloqueio;
import com.rafaela.estoque.domain.Reserva;
import com.rafaela.estoque.service.EstoqueService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/produtos/{produtoId}")
public class ProdutoController {

    private final EstoqueService service;

    public ProdutoController(EstoqueService service) {
        this.service = service;
    }

    @GetMapping
    public ProdutoResponse consultar(@PathVariable String produtoId) {
        return ProdutoResponse.from(service.consultarProduto(produtoId));
    }

    @PostMapping("/entradas")
    public ProdutoResponse registrarEntrada(@PathVariable String produtoId,
                                            @Valid @RequestBody QuantidadeRequest request) {
        return ProdutoResponse.from(service.registrarEntrada(produtoId, request.quantidade()));
    }

    @PostMapping("/saidas")
    public ProdutoResponse registrarSaida(@PathVariable String produtoId,
                                          @Valid @RequestBody QuantidadeRequest request) {
        return ProdutoResponse.from(service.registrarSaida(produtoId, request.quantidade()));
    }

    @PostMapping("/reservas")
    public ResponseEntity<ReservaResponse> reservar(@PathVariable String produtoId,
                                                    @Valid @RequestBody ReservaRequest request) {
        Reserva reserva = service.reservar(produtoId, request.solicitacaoId(), request.quantidade());
        return ResponseEntity.created(URI.create("/reservas/" + reserva.getId()))
                .body(ReservaResponse.from(reserva));
    }

    @PostMapping("/bloqueios")
    public ResponseEntity<BloqueioResponse> bloquear(@PathVariable String produtoId,
                                                     @Valid @RequestBody BloqueioRequest request) {
        Bloqueio bloqueio = service.bloquear(produtoId, request.motivo(), request.quantidade());
        return ResponseEntity.created(URI.create("/bloqueios/" + bloqueio.getId()))
                .body(BloqueioResponse.from(bloqueio));
    }
}