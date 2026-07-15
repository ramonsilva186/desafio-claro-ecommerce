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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final long MAX_ORDERS = 5L;

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;

    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll()
                .stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        OrderEntity order = findEntityById(id);
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        validateOrderLimit();

        OrderEntity order = orderMapper.toEntity(request);
        OrderEntity savedOrder = orderRepository.save(order);
        formatDisplayNameWithOrderId(savedOrder);

        log.info(
                "Pedido criado com sucesso: orderId={}, displayName={}, items={}, weight={}, status={}",
                savedOrder.getId(),
                savedOrder.getDisplayName(),
                savedOrder.getItems(),
                savedOrder.getWeight(),
                savedOrder.getStatus()
        );
        return orderMapper.toResponse(savedOrder);
    }

    @Transactional
    public OrderResponse updateStatus(Long id, UpdateOrderStatusRequest request) {
        OrderEntity order = findEntityById(id);
        OrderStatus currentStatus = order.getStatus();
        OrderStatus nextStatus = request.status();

        if (!currentStatus.canTransitionTo(nextStatus)) {
            log.warn(
                    "Tentativa de transicao invalida: orderId={}, currentStatus={}, nextStatus={}",
                    id,
                    currentStatus,
                    nextStatus
            );
            throw new BusinessExecption("Nao e permitido alterar o status de %s para %s".formatted(currentStatus, nextStatus));
        }

        order.setStatus(nextStatus);

        OrderEntity updatedOrder = orderRepository.save(order);

        log.info(
                "Status do pedido alterado com sucesso: orderId={}, previousStatus={}, currentStatus={}",
                updatedOrder.getId(),
                currentStatus,
                updatedOrder.getStatus()
        );
        return orderMapper.toResponse(updatedOrder);

    }

    @Transactional
    public void delete(Long id) {
        OrderEntity order = findEntityById(id);
        orderRepository.delete(order);

        log.info(
                "Pedido excluido com sucesso: orderId={}, displayName={}, status={}",
                order.getId(),
                order.getDisplayName(),
                order.getStatus()
        );
    }

    private void formatDisplayNameWithOrderId(OrderEntity order) {
        if (order.getId() == null || order.getDisplayName().startsWith("Pedido #")) {
            return;
        }

        order.setDisplayName("Pedido #%d - %s".formatted(order.getId(), order.getDisplayName().trim()));
    }

    private void validateOrderLimit() {
        long totalOrders = orderRepository.count();

        if (totalOrders >= MAX_ORDERS) {
            log.warn("Tentativa de cadastro bloqueada: totalOrders={}, maxOrders={}",
                    totalOrders,
                    MAX_ORDERS
            );

            throw new BusinessExecption("O limite maximo de %d pedidos foi atingido".formatted(MAX_ORDERS));
        }
    }

    private OrderEntity findEntityById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Pedido nao encontrado: orderId={}", id);
                    return new ResourceNotFoundExecption("Pedido com ID %d nao encontrado".formatted(id));
                });
    }

}
