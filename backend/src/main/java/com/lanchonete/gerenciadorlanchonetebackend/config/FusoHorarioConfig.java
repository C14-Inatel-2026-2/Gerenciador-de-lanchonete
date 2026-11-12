package com.lanchonete.gerenciadorlanchonetebackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.TimeZone;

/**
 * Fuso horário da lanchonete (#48). O Render roda em UTC: sem isto, a hora dos
 * pedidos ficaria 3h adiantada e o "dia" do painel virava às 21h de Brasília.
 *
 * A aplicação inteira roda em America/Sao_Paulo (aplicarComoPadrao, chamado no main),
 * e quem precisa saber "que dia é hoje" recebe o Clock, que nos testes vira um relógio fixo.
 */
@Configuration
public class FusoHorarioConfig {

    public static final ZoneId FUSO_DA_LANCHONETE = ZoneId.of("America/Sao_Paulo");

    public static void aplicarComoPadrao() {
        TimeZone.setDefault(TimeZone.getTimeZone(FUSO_DA_LANCHONETE));
    }

    @Bean
    public Clock relogio() {
        return Clock.system(FUSO_DA_LANCHONETE);
    }
}
