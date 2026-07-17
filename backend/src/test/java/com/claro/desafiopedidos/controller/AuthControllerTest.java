package com.claro.desafiopedidos.controller;

import com.claro.desafiopedidos.dto.request.LoginRequest;
import com.claro.desafiopedidos.dto.response.LoginResponse;
import com.claro.desafiopedidos.exception.GlobalExceptionHandler;
import com.claro.desafiopedidos.exception.UnauthorizedExecption;
import com.claro.desafiopedidos.security.JwtAuthenticationFilter;
import com.claro.desafiopedidos.service.AuthService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("POST /api/auth/login deve retornar token")
    void shouldLoginSuccessfully() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest(
                "admin@claro.com",
                "123456"
        );

        LoginResponse response = new LoginResponse(
                "jwt-token",
                "Bearer",
                3_600_000L
        );

        when(authService.login(request)).thenReturn(response);

        // Act + Assert
        mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiration").value(3_600_000L));

        verify(authService).login(request);
    }

    @Test
    @DisplayName("POST /api/auth/login deve retornar 401")
    void shouldReturnUnauthorizedForInvalidCredentials()
            throws Exception {

        // Arrange
        LoginRequest request = new LoginRequest(
                "admin@claro.com",
                "senha-errada"
        );

        when(authService.login(request)).thenThrow(new UnauthorizedExecption("Email ou senha inválidos"));

        // Act + Assert
        mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Email ou senha inválidos"));

        verify(authService).login(request);
    }

    @Test
    @DisplayName("POST /api/auth/login deve retornar 400 para email inválido")
    void shouldReturnBadRequestForInvalidEmail() throws Exception {
        // Arrange
        LoginRequest request = new LoginRequest(
                "email-invalido",
                "123456"
        );

        // Act + Assert
        mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.email").exists());

        verifyNoInteractions(authService);
    }
}