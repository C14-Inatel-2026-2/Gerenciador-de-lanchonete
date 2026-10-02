package com.lanchonete.gerenciadorlanchonetebackend.compras;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StatusPedidoTest {

    @ParameterizedTest(name = "{0} -> {1} é permitida")
    @CsvSource({
        "CRIADO,EM_PREPARO",
        "CRIADO,CANCELADO",
        "EM_PREPARO,PRONTO",
        "EM_PREPARO,CANCELADO",
        "PRONTO,ENTREGUE"
    })
    void devePermitirTransicoesValidas(StatusPedido origem, StatusPedido destino) {
        assertTrue(origem.podeTransitarPara(destino));
    }

    @ParameterizedTest(name = "{0} -> {1} é bloqueada")
    @CsvSource({
        "CRIADO,PRONTO",
        "CRIADO,ENTREGUE",
        "EM_PREPARO,CRIADO",
        "EM_PREPARO,ENTREGUE",
        "PRONTO,CRIADO",
        "PRONTO,EM_PREPARO",
        "PRONTO,CANCELADO"
    })
    void deveBloquearTransicoesInvalidas(StatusPedido origem, StatusPedido destino) {
        assertFalse(origem.podeTransitarPara(destino));
    }

    @ParameterizedTest(name = "estados finais não vão para {0}")
    @EnumSource(StatusPedido.class)
    void estadosFinaisNaoTransitamParaNenhumStatus(StatusPedido destino) {
        assertFalse(StatusPedido.ENTREGUE.podeTransitarPara(destino));
        assertFalse(StatusPedido.CANCELADO.podeTransitarPara(destino));
    }

    @Test
    void deveBloquearTransicaoParaStatusNulo() {
        assertFalse(StatusPedido.CRIADO.podeTransitarPara(null));
    }
}
