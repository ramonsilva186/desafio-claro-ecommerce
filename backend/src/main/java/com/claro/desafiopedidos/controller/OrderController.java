package com.claro.desafiopedidos.controller;

import com.claro.desafiopedidos.dto.request.CreateOrderRequest;
import com.claro.desafiopedidos.dto.request.UpdateOrderStatusRequest;
import com.claro.desafiopedidos.dto.response.OrderResponse;
import com.claro.desafiopedidos.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> findAll() {
        return ResponseEntity.ok(orderService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request){
        OrderResponse response = orderService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request){
        return ResponseEntity.ok(orderService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<OrderResponse> delete(@PathVariable Long id){
        orderService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
