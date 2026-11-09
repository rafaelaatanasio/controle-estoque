package com.rafaela.estoque.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BloqueioRequest(
        @NotBlank String motivo,
        @NotNull @Positive Integer quantidade
) {
}