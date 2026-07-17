package com.claro.desafiopedidos.entity.enums;

import java.util.List;

public enum OrderStatus {
    EM_PROCESSAMENTO,
    PAUSADO,
    CANCELADO;

    public boolean canTransitionTo(OrderStatus nextStatus) {
        if (nextStatus == null || this == nextStatus) {
            return false;
        }
        return switch (this) {
            case EM_PROCESSAMENTO -> List.of(PAUSADO, CANCELADO).contains(nextStatus);
            case PAUSADO -> List.of(EM_PROCESSAMENTO, CANCELADO).contains(nextStatus);
            case CANCELADO -> List.of(EM_PROCESSAMENTO).contains(nextStatus);
        };
    }
}
