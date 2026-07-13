package com.claro.desafiopedidos.repository;

import com.claro.desafiopedidos.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
}
