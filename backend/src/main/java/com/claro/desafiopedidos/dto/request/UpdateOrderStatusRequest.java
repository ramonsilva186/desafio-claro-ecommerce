package com.claro.desafiopedidos.dto.request;

import com.claro.desafiopedidos.entity.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateOrderStatusRequest(
        @NotNull(message = "O status do pedido é obrigatório")
        OrderStatus status
) {
}
