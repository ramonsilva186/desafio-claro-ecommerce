package com.claro.desafiopedidos.security;

import com.claro.desafiopedidos.dto.response.OrderResponse;
import com.claro.desafiopedidos.entity.enums.OrderStatus;
import com.claro.desafiopedidos.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "security.jwt.secret=12345678901234567890123456789012",
        "security.jwt.expiration=3600000",
        "security.jwt.issuer=desafio-pedidos-api"
})
@AutoConfigureMockMvc
class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @MockitoBean
    private OrderService orderService;

    @Test
    @DisplayName("Deve retornar 401 ao acessar pedidos sem token")
    void shouldReturnUnauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Deve permitir acesso com token valido")
    void shouldAllowRequestWithValidToken() throws Exception {
        UserDetails userDetails = User
                .withUsername("admin@claro.com")
                .password("hash")
                .authorities("USER")
                .build();

        String token = jwtService.generateToken(userDetails);

        OrderResponse response = new OrderResponse(
                1L,
                "Pedido Ramon Silva",
                3,
                1500,
                OrderStatus.EM_PROCESSAMENTO,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        when(orderService.findAll()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/pedidos")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].displayName").value("Pedido Ramon Silva"));
    }

    @Test
    @DisplayName("Deve retornar 401 para token invalido")
    void shouldReturnUnauthorizedForInvalidToken() throws Exception {
        mockMvc.perform(get("/api/pedidos")
                        .header("Authorization", "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Health deve continuar publico")
    void shouldAllowPublicHealthEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    @DisplayName("Metrics deve continuar publico")
    void shouldAllowPublicMetricsEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.names").isArray());
    }

    @Test
    @DisplayName("Info deve continuar publico")
    void shouldAllowPublicInfoEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Prometheus deve continuar publico")
    void shouldAllowPublicPrometheusEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/prometheus"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve permitir preflight CORS do frontend")
    void shouldAllowCorsPreflightFromFrontend() throws Exception {
        mockMvc.perform(options("/api/auth/login")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:4200"))
                .andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,PATCH,DELETE,OPTIONS"));
    }
}
