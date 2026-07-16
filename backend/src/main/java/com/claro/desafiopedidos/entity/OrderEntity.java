package com.claro.desafiopedidos.entity;

import com.claro.desafiopedidos.entity.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "display_name", length = 150)
    private String displayName;

    @Column(nullable = false, name = "items")
    private Integer items;

    @Column(nullable = false, name = "weight")
    private Integer weight;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "status", length = 30)
    private OrderStatus status;

    @Column(nullable = false, updatable = false, name = "created_at")
    private LocalDateTime  createdAt;

    @Column(nullable = false, name = "updated_at")
    private LocalDateTime  updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();

        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public OrderEntity(String displayName, Integer items, Integer weight) {
        this.displayName = displayName;
        this.items = items;
        this.weight = weight;
        this.status = OrderStatus.EM_PROCESSAMENTO;
    }

    public Integer getTotalWeight() {
        return this.weight * this.items;
    }
}
