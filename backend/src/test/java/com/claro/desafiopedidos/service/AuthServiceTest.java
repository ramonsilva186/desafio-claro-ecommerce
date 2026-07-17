package com.claro.desafiopedidos.service;

import com.claro.desafiopedidos.config.JwtProperties;
import com.claro.desafiopedidos.dto.request.LoginRequest;
import com.claro.desafiopedidos.dto.response.LoginResponse;
import com.claro.desafiopedidos.exception.UnauthorizedExecption;
import com.claro.desafiopedidos.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private Authentication authentication;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        JwtProperties jwtProperties = new JwtProperties(
                "12345678901234567890123456789012",
                3_600_000L,
                "desafio-pedidos-api"
        );
        authService = new AuthService(jwtProperties, authenticationManager, jwtService);
    }

    @Test
    @DisplayName("Deve autenticar usuário e retornar JWT")
    void shouldAuthenticateAndReturnToken() {
        // Arrange
        LoginRequest request = new LoginRequest(
                "admin@claro.com",
                "123456"
        );

        UserDetails userDetails = new User(
                "admin@claro.com",
                "hash",
                List.of());

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        // Act
        LoginResponse response = authService.login(request);

        // Assert
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.expiration()).isEqualTo(3_600_000L);

        verify(authenticationManager).authenticate(
                argThat(auth ->
                        auth.getPrincipal().equals("admin@claro.com")
                                && auth.getCredentials().equals("123456")));

        verify(jwtService).generateToken(userDetails);
    }

    @Test
    @DisplayName("Deve retornar erro para credenciais inválidas")
    void shouldRejectInvalidCredentials() {
        // Arrange
        LoginRequest request = new LoginRequest(
                "admin@claro.com",
                "senha-errada"
        );

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        // Act + Assert
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(UnauthorizedExecption.class)
                .hasMessage("Email ou senha inválidos");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        verifyNoInteractions(jwtService);
    }
}