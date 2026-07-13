package com.claro.desafiopedidos.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users_ecommerce")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "email", unique = true,  nullable = false,  length = 150)
    private String email;

    @Column(name = "password", nullable = false, length = 150)
    private String password;
}
