package com.claro.desafiopedidos;

import com.claro.desafiopedidos.config.JwtProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(JwtProperties.class)
public class DesafioPedidosApplication {

	public static void main(String[] args) {
		SpringApplication.run(DesafioPedidosApplication.class, args);
	}

}
