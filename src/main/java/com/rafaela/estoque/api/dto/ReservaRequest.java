package com.rafaela.estoque.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReservaRequest(
        @NotBlank String solicitacaoId,
        @NotNull @Positive Integer quantidade
) {
}