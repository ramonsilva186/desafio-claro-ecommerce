package com.claro.desafiopedidos.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "O email é obrigatório")
        @Email(message = "Informe um email válido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        String password
) {}
