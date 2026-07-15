package com.claro.desafiopedidos.service;

import com.claro.desafiopedidos.dto.request.CreateOrderRequest;
import com.claro.desafiopedidos.dto.request.UpdateOrderStatusRequest;
import com.claro.desafiopedidos.dto.response.OrderResponse;
import com.claro.desafiopedidos.entity.OrderEntity;
import com.claro.desafiopedidos.entity.enums.OrderStatus;
import com.claro.desafiopedidos.exception.BusinessExecption;
import com.claro.desafiopedidos.exception.ResourceNotFoundExecption;
import com.claro.desafiopedidos.mapper.OrderMapper;
import com.claro.desafiopedidos.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(orderRepository, orderMapper);
    }

    @Test
    @DisplayName("Deve listar todos os pedidos")
    void shouldReturnAllOrders() {
        // Arrange
        OrderEntity order = createOrder();
        OrderResponse response = createResponse(1L, OrderStatus.EM_PROCESSAMENTO);

        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(orderMapper.toResponse(order)).thenReturn(response);
        // Act
        List<OrderResponse> result = orderService.findAll();
        // Assert
        assertThat(result).hasSize(1).containsExactly(response);

        verify(orderRepository).findAll();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando nÃ£o houver pedidos")
    void shouldReturnEmptyList() {
        // Arrange
        when(orderRepository.findAll()).thenReturn(List.of());

        // Act
        List<OrderResponse> result = orderService.findAll();

        // Assert
        assertThat(result).isEmpty();
        verify(orderRepository).findAll();
    }

    @Test
    @DisplayName("Deve buscar pedido por ID")
    void shouldFindOrderById() {
        // Arrange
        OrderEntity order = createOrder();

        OrderResponse response = createResponse(1L, OrderStatus.EM_PROCESSAMENTO);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        when(orderMapper.toResponse(order)).thenReturn(response);

        // Act
        OrderResponse result = orderService.findById(1L);

        // Assert
        assertThat(result).isEqualTo(response);

        verify(orderRepository).findById(1L);
    }

    @Test
    @DisplayName("Deve lanÃ§ar exceÃ§Ã£o ao buscar pedido inexistente")
    void shouldThrowWhenOrderDoesNotExist() {
        // Arrange
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());
        // Act + Assert
        assertThatThrownBy(() -> orderService.findById(99L))
                .isInstanceOf(ResourceNotFoundExecption.class).
                hasMessage("Pedido com ID 99 nao encontrado");

        verify(orderRepository).findById(99L);
    }

    @Test
    @DisplayName("Deve criar pedido com status EM_PROCESSAMENTO")
    void shouldCreateOrder() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest("Ramon Silva", 3, 1500);

        OrderEntity order = new OrderEntity(request.displayName(), request.items(), request.weight());
        order.setId(4L);

        OrderResponse response = new OrderResponse(
                4L,
                "Pedido #4 - Ramon Silva",
                3,
                1500,
                OrderStatus.EM_PROCESSAMENTO,
                LocalDateTime.of(2026, 7, 14, 10, 30),
                LocalDateTime.of(2026, 7, 14, 10, 30)
        );

        when(orderRepository.count()).thenReturn(3L);
        when(orderMapper.toEntity(request)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(response);

        // Act
        OrderResponse result = orderService.create(request);

        // Assert
        assertThat(result).isEqualTo(response);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.EM_PROCESSAMENTO);
        assertThat(order.getDisplayName()).isEqualTo("Pedido #4 - Ramon Silva");

        verify(orderRepository).count();
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("Deve impedir criaÃ§Ã£o do sexto pedido")
    void shouldBlockSixthOrder() {
        // Arrange
        CreateOrderRequest request = new CreateOrderRequest("Ramon Silva", 3, 1500);

        when(orderRepository.count()).thenReturn(5L);

        // Act + Assert
        assertThatThrownBy(() -> orderService.create(request))
                .isInstanceOf(BusinessExecption.class)
                .hasMessage("O limite maximo de 5 pedidos foi atingido");

        verify(orderRepository).count();
        verify(orderRepository, never()).save(any());
        verify(orderMapper, never()).toEntity(any());
    }

    @Test
    @DisplayName("Deve alterar status quando a transiÃ§Ã£o for vÃ¡lida")
    void shouldUpdateStatus() {
        // Arrange
        OrderEntity order = createOrder();

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.PAUSADO);

        OrderResponse response = createResponse(1L, OrderStatus.PAUSADO);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(response);

        // Act
        OrderResponse result = orderService.updateStatus(1L, request);

        // Assert
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PAUSADO);
        assertThat(result.status()).isEqualTo(OrderStatus.PAUSADO);

        verify(orderRepository).findById(1L);
        verify(orderRepository).save(order);
    }

    @Test
    @DisplayName("Deve impedir transiÃ§Ã£o de status invÃ¡lida")
    void shouldBlockInvalidStatusTransition() {
        // Arrange
        OrderEntity order = createOrder();
        order.setStatus(OrderStatus.CANCELADO);

        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.PAUSADO);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act + Assert
        assertThatThrownBy(
                () -> orderService.updateStatus(1L, request))
                .isInstanceOf(BusinessExecption.class)
                .hasMessage("Nao e permitido alterar o status de CANCELADO para PAUSADO");

        assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELADO);

        verify(orderRepository).findById(1L);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lanÃ§ar exceÃ§Ã£o ao alterar pedido inexistente")
    void shouldThrowWhenUpdatingMissingOrder() {
        // Arrange
        UpdateOrderStatusRequest request = new UpdateOrderStatusRequest(OrderStatus.PAUSADO);

        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(
                () -> orderService.updateStatus(99L, request))
                .isInstanceOf(ResourceNotFoundExecption.class)
                .hasMessage("Pedido com ID 99 nao encontrado");

        verify(orderRepository).findById(99L);
        verify(orderRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve excluir pedido existente")
    void shouldDeleteOrder() {
        // Arrange
        OrderEntity order = createOrder();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act
        orderService.delete(1L);

        // Assert
        verify(orderRepository).findById(1L);
        verify(orderRepository).delete(order);
    }

    @Test
    @DisplayName("Deve lanÃ§ar exceÃ§Ã£o ao excluir pedido inexistente")
    void shouldThrowWhenDeletingMissingOrder() {
        // Arrange
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        // Act + Assert
        assertThatThrownBy(() -> orderService.delete(99L))
                .isInstanceOf(ResourceNotFoundExecption.class)
                .hasMessage("Pedido com ID 99 nao encontrado");

        verify(orderRepository).findById(99L);
        verify(orderRepository, never()).delete(any());
    }

    private OrderEntity createOrder() {
        return new OrderEntity("Pedido Ramon Silva", 3, 1500);
    }

    private OrderResponse createResponse(Long id, OrderStatus status) {
        LocalDateTime now = LocalDateTime.of(2026, 7, 14, 10, 30);

        return new OrderResponse(
                id, "Pedido Ramon Silva", 3, 1500, status, now, now
        );
    }
}