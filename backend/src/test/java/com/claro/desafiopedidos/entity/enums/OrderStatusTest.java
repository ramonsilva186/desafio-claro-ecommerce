package com.claro.desafiopedidos.entity.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @ParameterizedTest(name = "{0} deve permitir transição para {1}")
    @MethodSource("allowedTransitions")
    @DisplayName("Deve permitir as transições válidas")
    void shouldAllowValidTransitions(OrderStatus currentStatus, OrderStatus nextStatus) {
        boolean result = currentStatus.canTransitionTo(nextStatus);
        assertThat(result).isTrue();
    }

    @ParameterizedTest(name = "{0} não deve permitir transição para {1}")
    @MethodSource("notAllowedTransitions")
    @DisplayName("Deve bloquear as transições inválidas")
    void shouldBlockInvalidTransitions(OrderStatus currentStatus, OrderStatus nextStatus) {
        boolean result = currentStatus.canTransitionTo(nextStatus);
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Não deve permitir transição para o mesmo status")
    void shouldNotAllowTransitionToSameStatus() {
        assertThat(OrderStatus.EM_PROCESSAMENTO
                .canTransitionTo(OrderStatus.EM_PROCESSAMENTO))
                .isFalse();

        assertThat(
                OrderStatus.PAUSADO.canTransitionTo(OrderStatus.PAUSADO)).isFalse();

        assertThat(
                OrderStatus.CANCELADO.canTransitionTo(OrderStatus.CANCELADO)).isFalse();
    }

    @Test
    @DisplayName("Não deve permitir transição para status nulo")
    void shouldNotAllowTransitionToNull() {
        assertThat(OrderStatus.EM_PROCESSAMENTO.canTransitionTo(null)).isFalse();
    }

    private static Stream<Arguments> allowedTransitions() {
        return Stream.of(
                Arguments.of(OrderStatus.EM_PROCESSAMENTO, OrderStatus.PAUSADO),
                Arguments.of(OrderStatus.EM_PROCESSAMENTO, OrderStatus.CANCELADO),
                Arguments.of(OrderStatus.PAUSADO, OrderStatus.EM_PROCESSAMENTO),
                Arguments.of(OrderStatus.PAUSADO, OrderStatus.CANCELADO),
                Arguments.of(OrderStatus.CANCELADO, OrderStatus.EM_PROCESSAMENTO)
        );
    }

    private static Stream<Arguments> notAllowedTransitions() {
        return Stream.of(
                Arguments.of(OrderStatus.CANCELADO, OrderStatus.PAUSADO)
        );
    }
}