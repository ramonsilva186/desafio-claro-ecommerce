package com.claro.desafiopedidos.dto.response;

import com.claro.desafiopedidos.entity.enums.OrderStatus;

import java.time.LocalDateTime;

public record OrderResponse(

        Long id,
        String displayName,
        Integer items,
        Integer weight,
        OrderStatus status,
        LocalDateTime createAt,
        LocalDateTime updateAt
) {
}
