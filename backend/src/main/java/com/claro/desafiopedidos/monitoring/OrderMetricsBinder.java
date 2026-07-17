package com.claro.desafiopedidos.monitoring;

import com.claro.desafiopedidos.entity.enums.OrderStatus;
import com.claro.desafiopedidos.repository.OrderRepository;
import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderMetricsBinder implements MeterBinder {

    private static final String DESCRIPTION = "Metricas de negocio dos pedidos";

    private final OrderRepository orderRepository;

    @Override
    public void bindTo(MeterRegistry registry) {
        FunctionCounter.builder("pedidos", orderRepository, OrderRepository::count)
                .description(DESCRIPTION)
                .register(registry);

        Gauge.builder("pedidos_current", orderRepository, OrderRepository::count)
                .description(DESCRIPTION)
                .register(registry);

        for (OrderStatus status : OrderStatus.values()) {
            Gauge.builder("pedidos_by_status", orderRepository, repository -> repository.countByStatus(status))
                    .tag("status", status.name())
                    .description(DESCRIPTION)
                    .register(registry);
        }
    }
}