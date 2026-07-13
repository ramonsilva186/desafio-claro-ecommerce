package com.claro.desafiopedidos.dto.request;

import jakarta.validation.constraints.*;

public record CreateOrderRequest(
        @NotBlank(message = "O displayName é obrigatório")
        @Size(min = 5, max = 150, message = "O displayName deve ter entre 5 e 150 caracteres")
        String displayName,

        @NotNull(message = "A quantidade de itens é obrigatória")
        @Min(value = 1, message = "A quantidade de itens deve ser maior que 0")
        Integer items,

        @NotNull(message = "O peso é obrigatório")
        @Positive(message = "O peso deve ser maior que 0")
        Integer weight

) {
}
