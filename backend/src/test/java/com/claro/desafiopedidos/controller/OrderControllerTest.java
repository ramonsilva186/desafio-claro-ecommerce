package com.claro.desafiopedidos.controller;

import com.claro.desafiopedidos.dto.request.CreateOrderRequest;
import com.claro.desafiopedidos.dto.request.UpdateOrderStatusRequest;
import com.claro.desafiopedidos.dto.response.OrderResponse;
import com.claro.desafiopedidos.entity.enums.OrderStatus;
import com.claro.desafiopedidos.exception.*;
import com.claro.desafiopedidos.security.JwtAuthenticationFilter;
import com.claro.desafiopedidos.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;


import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @DisplayName("GET /api/pedidos deve retornar 200")
    void shouldReturnAllOrders() throws Exception {
        // Arrange
        OrderResponse response = createResponse(OrderStatus.EM_PROCESSAMENTO);
        when(orderService.findAll()).thenReturn(List.of(response));

        // Act + Assert
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].displayName").value("Pedido Ramon Silva"))
                .andExpect(jsonPath("$[0].items").value(3))
                .andExpect(jsonPath("$[0].weight").value(1500))
                .andExpect(jsonPath("$[0].status").value("EM_PROCESSAMENTO"));

        verify(orderService).findAll();
    }

    @Test
    @DisplayName("GET /api/pedidos/{id} deve retornar 200")
    void shouldReturnOrderById() throws Exception {
        // Arrange
        OrderResponse response = createResponse(OrderStatus.EM_PROCESSAMENTO);
        when(orderService.findById(1L)).thenReturn(response);

        // Act + Assert
        mockMvc.perform(get("/api/pedidos/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.displayName").value("Pedido Ramon Silva"));

        verify(orderService).findById(1L);
    }

    @Test
    @DisplayName("GET /api/pedidos/{id} deve retornar 404")
    void shouldReturnNotFound() throws Exception {
        // Arrange
        when(orderService.findById(99L)).thenThrow(new ResourceNotFoundExecption("Pedido com ID 99 não encontrado"));

        // Act + Assert
        mockMvc.perform(get("/api/pedidos/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Pedido com ID 99 não encontrado"));

        verify(orderService).findById(99L);
    }

    @Test
    @DisplayName("POST /api/pedidos deve retornar 201")
    void shouldCreateOrder() throws Exception {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest("Pedido Ramon Silva", 3, 1500);
        OrderResponse response = createResponse(OrderStatus.EM_PROCESSAMENTO);
        when(orderService.create(request)).thenReturn(response);

        // Act + Assert
        mockMvc.perform(post("/api/pedidos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.displayName").value("Pedido Ramon Silva"))
                .andExpect(jsonPath("$.status").value("EM_PROCESSAMENTO"));

        verify(orderService).create(request);
    }

    @Test
    @DisplayName("POST /api/pedidos deve retornar 400 para dados inválidos")
    void shouldReturnBadRequestForInvalidRequest() throws Exception {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest("Ana", 0, -1);

        // Act + Assert
        mockMvc.perform(post("/api/pedidos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Existem campos inválidos na requisição"))
                .andExpect(jsonPath("$.fieldErrors.displayName").exists())
                .andExpect(jsonPath("$.fieldErrors.items").exists())
                .andExpect(jsonPath("$.fieldErrors.weight").exists());

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName("POST /api/pedidos deve retornar 422 ao atingir limite")
    void shouldReturnUnprocessableEntityWhenLimitIsReached() throws Exception {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest("Pedido Ramon Silva", 3, 1500);
        when(orderService.create(request)).thenThrow(new BusinessExecption("O limite máximo de 5 pedidos foi atingido"));

        // Act + Assert
        mockMvc.perform(post("/api/pedidos")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message").value("O limite máximo de 5 pedidos foi atingido"));

        verify(orderService).create(request);
    }

    @Test
    @DisplayName("PATCH /api/pedidos/{id}/status deve retornar 200")
    void shouldUpdateStatus() throws Exception {
        // Arrange
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.PAUSADO);
        OrderResponse response = createResponse(OrderStatus.PAUSADO);
        when(orderService.updateStatus(1L, request)).thenReturn(response);

        // Act + Assert
        mockMvc.perform(patch("/api/pedidos/{id}/status", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("PAUSADO"));

        verify(orderService).updateStatus(1L, request);
    }

    @Test
    @DisplayName("PATCH deve retornar 422 para transição inválida")
    void shouldReturnUnprocessableEntityForInvalidTransition() throws Exception {
        // Arrange
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.PAUSADO);

        when(orderService.updateStatus(1L, request))
                .thenThrow(new BusinessExecption("Não é permitido alterar o status de CANCELADO para PAUSADO"));

        // Act + Assert
        mockMvc.perform(patch("/api/pedidos/{id}/status", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.message")
                                .value("Não é permitido alterar o status de CANCELADO para PAUSADO"));

        verify(orderService).updateStatus(1L, request);
    }

    @Test
    @DisplayName("PATCH deve retornar 400 para status inexistente")
    void shouldReturnBadRequestForUnknownStatus() throws Exception {
        String invalidBody = """
                {
                    "status": "FINALIZADO"
                }
                """;

        mockMvc.perform(patch("/api/pedidos/{id}/status", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                                .value("O corpo da requisição está inválido ou possui valores não reconhecidos")
                );

        verifyNoInteractions(orderService);
    }

    @Test
    @DisplayName("DELETE /api/pedidos/{id} deve retornar 204")
    void shouldDeleteOrder() throws Exception {
        // Arrange
        doNothing().when(orderService).delete(1L);

        // Act + Assert
        mockMvc.perform(delete("/api/pedidos/{id}", 1L))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        verify(orderService).delete(1L);
    }

    private OrderResponse createResponse(OrderStatus status) {
        LocalDateTime now = LocalDateTime.of(2026, 7, 14, 10, 30);
        return new OrderResponse(1L, "Pedido Ramon Silva", 3, 1500, status, now, now);
    }
}