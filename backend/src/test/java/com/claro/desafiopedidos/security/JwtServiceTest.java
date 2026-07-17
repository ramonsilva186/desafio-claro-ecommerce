package com.claro.desafiopedidos.security;

import com.claro.desafiopedidos.config.JwtProperties;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET =
            "12345678901234567890123456789012";

    private UserDetails userDetails;

    @BeforeEach
    void setUp() {
        userDetails = new User("admin@claro.com", "hash", List.of());
    }

    @Test
    @DisplayName("Deve gerar token e extrair email")
    void shouldGenerateTokenAndExtractUsername() {
        // Arrange
        JwtProperties properties = new JwtProperties(SECRET, 3_600_000L, "desafio-pedidos-api");

        JwtService jwtService = new JwtService(properties);

        // Act
        String token = jwtService.generateToken(userDetails);
        String username = jwtService.extractUsername(token);

        // Assert
        assertThat(token).isNotBlank();
        assertThat(username).isEqualTo("admin@claro.com");
        assertThat(jwtService.isTokenValid(token, userDetails)).isTrue();
    }

    @Test
    @DisplayName("Deve considerar inválido token de outro usuário")
    void shouldRejectTokenForDifferentUser() {
        // Arrange
        JwtProperties properties = new JwtProperties(SECRET, 3_600_000L, "desafio-pedidos-api");

        JwtService jwtService = new JwtService(properties);
        String token = jwtService.generateToken(userDetails);
        UserDetails anotherUser = new User("outro@claro.com", "hash", List.of());

        // Act
        boolean valid = jwtService.isTokenValid(token, anotherUser);
        // Assert
        assertThat(valid).isFalse();
    }

    @Test
    @DisplayName("Deve rejeitar token expirado")
    void shouldRejectExpiredToken() throws InterruptedException {
        // Arrange
        JwtProperties properties = new JwtProperties(SECRET, 1L, "desafio-pedidos-api");

        JwtService jwtService = new JwtService(properties);
        String token = jwtService.generateToken(userDetails);
        Thread.sleep(10);

        // Act + Assert
        assertThatThrownBy(() -> jwtService.extractUsername(token)).isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    @DisplayName("Deve rejeitar token assinado com outra chave")
    void shouldRejectTokenWithDifferentSecret() {
        // Arrange
        JwtService tokenIssuer = new JwtService(
                new JwtProperties(SECRET, 3_600_000L, "desafio-pedidos-api"));

        JwtService tokenValidator = new JwtService(
                new JwtProperties(
                        "abcdefghijklmnopqrstuvwxyz123456",
                        3_600_000L,
                        "desafio-pedidos-api"));
        String token = tokenIssuer.generateToken(userDetails);

        // Act + Assert
        assertThatThrownBy(() -> tokenValidator.extractUsername(token)).isInstanceOf(RuntimeException.class);
    }
}