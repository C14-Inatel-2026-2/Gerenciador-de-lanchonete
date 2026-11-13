package com.lanchonete.gerenciadorlanchonetebackend;

import com.lanchonete.gerenciadorlanchonetebackend.config.FusoHorarioConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GerenciadorLanchoneteBackendApplication {
    public static void main(String[] args) {
        // Antes de tudo: a hora dos pedidos é gravada no fuso de Brasília, mesmo no Render (UTC)
        FusoHorarioConfig.aplicarComoPadrao();
        SpringApplication.run(GerenciadorLanchoneteBackendApplication.class, args);
    }
}

