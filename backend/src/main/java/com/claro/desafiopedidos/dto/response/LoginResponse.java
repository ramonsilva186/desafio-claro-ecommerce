package com.claro.desafiopedidos.dto.response;


public record LoginResponse(
        String token,
        String tokenType,
        long expiration
) {
    public LoginResponse(String token) {
        this(token, "Bearer", 3600000);
    }
}
