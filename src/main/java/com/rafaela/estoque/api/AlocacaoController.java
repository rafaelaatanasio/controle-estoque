package com.rafaela.estoque.api;

import com.rafaela.estoque.api.dto.BloqueioResponse;
import com.rafaela.estoque.api.dto.ReservaResponse;
import com.rafaela.estoque.service.EstoqueService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class AlocacaoController {

    private final EstoqueService service;

    public AlocacaoController(EstoqueService service) {
        this.service = service;
    }

    @GetMapping("/reservas/{id}")
    public ReservaResponse consultarReserva(@PathVariable UUID id) {
        return ReservaResponse.from(service.consultarReserva(id));
    }

    @PostMapping("/reservas/{id}/liberacao")
    public ReservaResponse liberarReserva(@PathVariable UUID id) {
        return ReservaResponse.from(service.liberarReserva(id));
    }

    @GetMapping("/bloqueios/{id}")
    public BloqueioResponse consultarBloqueio(@PathVariable UUID id) {
        return BloqueioResponse.from(service.consultarBloqueio(id));
    }

    @PostMapping("/bloqueios/{id}/liberacao")
    public BloqueioResponse liberarBloqueio(@PathVariable UUID id) {
        return BloqueioResponse.from(service.liberarBloqueio(id));
    }
}