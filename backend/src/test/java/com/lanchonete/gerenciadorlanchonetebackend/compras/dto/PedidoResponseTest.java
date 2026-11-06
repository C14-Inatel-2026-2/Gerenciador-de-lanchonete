package com.lanchonete.gerenciadorlanchonetebackend.compras.dto;

import com.lanchonete.gerenciadorlanchonetebackend.compras.model.ItemPedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.Pedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.StatusPedido;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PedidoResponseTest {

    @Test
    void converteDadosDoPedidoECalculaSubtotalDeCadaItem() {
        Pedido pedido = new Pedido(10L, List.of(
            new ItemPedido(1L, "Hamburguer", new BigDecimal("12.50"), 2),
            new ItemPedido(2L, "Batata", new BigDecimal("6.00"), 1)
        ));
        pedido.setId(5L);

        PedidoResponse response = PedidoResponse.de(pedido);

        assertEquals(5L, response.id());
        assertEquals(10L, response.clienteId());
        assertEquals(StatusPedido.CRIADO, response.status());
        assertEquals(new BigDecimal("31.00"), response.total());
        assertEquals(pedido.getCriadoEm(), response.criadoEm());
        assertEquals(2, response.itens().size());

        ItemPedidoResponse primeiro = response.itens().get(0);
        assertEquals(1L, primeiro.produtoId());
        assertEquals("Hamburguer", primeiro.nome());
        assertEquals(new BigDecimal("12.50"), primeiro.precoUnitario());
        assertEquals(2, primeiro.quantidade());
        assertEquals(new BigDecimal("25.00"), primeiro.subtotal());
    }
}