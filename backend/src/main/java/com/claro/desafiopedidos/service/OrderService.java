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
        List<OrderResponse> orders = orderRepository.findAll()
                .stream()
                .map(orderMapper::toResponse)
                .toList();

        log.info("event=orders_listed totalOrders={}", orders.size());
        return orders;
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        OrderEntity order = findEntityById(id);

        log.info("event=order_retrieved orderId={} status={}", order.getId(), order.getStatus());
        return orderMapper.toResponse(order);
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        validateOrderLimit();

        OrderEntity order = orderMapper.toEntity(request);
        OrderEntity savedOrder = orderRepository.save(order);
        formatDisplayNameWithOrderId(savedOrder);

        log.info(
                "event=order_created orderId={} displayName={} items={} weightGrams={} status={}",
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
                    "event=order_status_transition_denied orderId={} currentStatus={} requestedStatus={}",
                    id,
                    currentStatus,
                    nextStatus
            );
            throw new BusinessExecption("Nao e permitido alterar o status de %s para %s".formatted(currentStatus, nextStatus));
        }

        order.setStatus(nextStatus);

        OrderEntity updatedOrder = orderRepository.save(order);

        log.info(
                "event=order_status_updated orderId={} previousStatus={} currentStatus={}",
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
                "event=order_deleted orderId={} displayName={} status={}",
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
            log.warn(
                    "event=order_creation_blocked reason=max_orders_reached totalOrders={} maxOrders={}",
                    totalOrders,
                    MAX_ORDERS
            );

            throw new BusinessExecption("O limite maximo de %d pedidos foi atingido".formatted(MAX_ORDERS));
        }
    }

    private OrderEntity findEntityById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("event=order_not_found orderId={}", id);
                    return new ResourceNotFoundExecption("Pedido com ID %d nao encontrado".formatted(id));
                });
    }
}
