package com.claro.desafiopedidos.repository;

import com.claro.desafiopedidos.entity.OrderEntity;
import com.claro.desafiopedidos.entity.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    long countByStatus(OrderStatus status);
}