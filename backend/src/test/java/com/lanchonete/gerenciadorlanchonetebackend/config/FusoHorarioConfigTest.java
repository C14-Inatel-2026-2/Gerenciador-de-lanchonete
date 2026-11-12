package com.lanchonete.gerenciadorlanchonetebackend.config;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.util.TimeZone;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FusoHorarioConfigTest {

    @Test
    void relogioDaAplicacaoUsaOFusoDeBrasilia() {
        assertEquals(ZoneId.of("America/Sao_Paulo"), new FusoHorarioConfig().relogio().getZone());
    }

    @Test
    void aplicarComoPadraoMudaOFusoDaJvm() {
        TimeZone original = TimeZone.getDefault();
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            FusoHorarioConfig.aplicarComoPadrao();

            assertEquals("America/Sao_Paulo", TimeZone.getDefault().getID());
        } finally {
            // Não deixa o fuso alterado para os outros testes
            TimeZone.setDefault(original);
        }
    }
}
