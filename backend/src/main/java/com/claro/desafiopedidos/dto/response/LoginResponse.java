package com.claro.desafiopedidos.dto.response;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginResponse(
        String token,
        String tokenType
) {
    public LoginResponse(String token) {
        this(token, "Bearer");
    }
}
