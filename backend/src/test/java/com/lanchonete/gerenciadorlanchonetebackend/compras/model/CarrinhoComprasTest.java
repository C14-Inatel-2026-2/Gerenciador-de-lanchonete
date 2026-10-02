package com.lanchonete.gerenciadorlanchonetebackend.compras.model;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Categoria;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Produto;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CarrinhoComprasTest {
	
    @Test
	void adicionarItemInsereNovoProduto() {
		CarrinhoCompras carrinho = new CarrinhoCompras();
		ItemCarrinho item = novoItem(1, "Hamburguer", "12.50", 2);

		carrinho.adicionarItem(item);

		assertEquals(1, carrinho.obterQuantidadeDeProdutosDiferentes());
		assertEquals(2, carrinho.obterQuantidadeTotalDeItens());
		assertTrue(carrinho.contemItem(item));
	}

	@Test
	void adicionarItemSomaQuantidadeQuandoProdutoJaExiste() {
		CarrinhoCompras carrinho = new CarrinhoCompras();
		carrinho.adicionarItem(novoItem(1, "Hamburguer", "12.50", 2));

		carrinho.adicionarItem(novoItem(1, "Hamburguer", "12.50", 3));

		assertEquals(1, carrinho.obterQuantidadeDeProdutosDiferentes());
		assertEquals(5, carrinho.obterQuantidadeTotalDeItens());
	}

	@Test
	void adicionarItemNuloLancaExcecao() {
		CarrinhoCompras carrinho = new CarrinhoCompras();

		assertThrows(IllegalArgumentException.class, () -> carrinho.adicionarItem(null));
	}

	@Test
	void removerItemRemoveProdutoExistente() {
		CarrinhoCompras carrinho = new CarrinhoCompras();
		ItemCarrinho item = novoItem(1, "Hamburguer", "12.50", 1);
		carrinho.adicionarItem(item);

		boolean removido = carrinho.removerItem(item);

		assertTrue(removido);
		assertTrue(carrinho.estaVazio());
	}

	@Test
	void removerItemRetornaFalseQuandoProdutoNaoExiste() {
		CarrinhoCompras carrinho = new CarrinhoCompras();
		carrinho.adicionarItem(novoItem(1, "Hamburguer", "12.50", 1));

		assertFalse(carrinho.removerItem(novoItem(2, "Batata", "6.00", 1)));
	}

	@Test
	void removerItemNuloLancaExcecao() {
		CarrinhoCompras carrinho = new CarrinhoCompras();

		assertThrows(IllegalArgumentException.class, () -> carrinho.removerItem(null));
	}

	@Test
	void removerItemPorProdutoIdRemoveProdutoExistente() {
		CarrinhoCompras carrinho = new CarrinhoCompras();
		carrinho.adicionarItem(novoItem(1, "Hamburguer", "12.50", 1));

		assertTrue(carrinho.removerItemPorProdutoId(1L));
		assertTrue(carrinho.estaVazio());
	}

	@Test
	void removerItemPorProdutoIdRetornaFalseQuandoProdutoNaoExiste() {
		CarrinhoCompras carrinho = new CarrinhoCompras();

		assertFalse(carrinho.removerItemPorProdutoId(1L));
	}

	@Test
	void removerItemPorProdutoIdRejeitaIdentificadorInvalido() {
		CarrinhoCompras carrinho = new CarrinhoCompras();

		assertThrows(IllegalArgumentException.class, () -> carrinho.removerItemPorProdutoId(null));
		assertThrows(IllegalArgumentException.class, () -> carrinho.removerItemPorProdutoId(0L));
	}

	@Test
	void alterarQuantidadeAtualizaQuantidadeDoProduto() {
		CarrinhoCompras carrinho = new CarrinhoCompras();
		carrinho.adicionarItem(novoItem(1, "Hamburguer", "12.50", 2));

		carrinho.alterarQuantidade(1L, 4);

		assertEquals(4, carrinho.obterQuantidadeTotalDeItens());
	}

	@Test
	void alterarQuantidadeRemoveProdutoQuandoNovaQuantidadeNaoEPositiva() {
		CarrinhoCompras carrinho = new CarrinhoCompras();
		carrinho.adicionarItem(novoItem(1, "Hamburguer", "12.50", 2));

		carrinho.alterarQuantidade(1L, 0);

		assertTrue(carrinho.estaVazio());
	}

	@Test
	void alterarQuantidadeDeProdutoInexistenteLancaExcecao() {
		CarrinhoCompras carrinho = new CarrinhoCompras();

		assertThrows(IllegalArgumentException.class, () -> carrinho.alterarQuantidade(1L, 2));
	}

	@Test
	void listarItensRetornaListaImutavel() {
		CarrinhoCompras carrinho = new CarrinhoCompras();
		carrinho.adicionarItem(novoItem(1, "Hamburguer", "12.50", 1));
		List<ItemCarrinho> itens = carrinho.listarItens();

		assertThrows(UnsupportedOperationException.class, itens::clear);
		assertEquals(1, carrinho.obterQuantidadeDeProdutosDiferentes());
	}

	@Test
	void obterQuantidadeDeProdutosDiferentesContaProdutosUnicos() {
		CarrinhoCompras carrinho = new CarrinhoCompras();
		carrinho.adicionarItem(novoItem(1, "Hamburguer", "12.50", 2));
		carrinho.adicionarItem(novoItem(2, "Batata", "6.00", 1));

		assertEquals(2, carrinho.obterQuantidadeDeProdutosDiferentes());
	}

	@Test
	void obterQuantidadeTotalDeItensSomaAsQuantidades() {
		CarrinhoCompras carrinho = new CarrinhoCompras();
		carrinho.adicionarItem(novoItem(1, "Hamburguer", "12.50", 2));
		carrinho.adicionarItem(novoItem(2, "Batata", "6.00", 3));

		assertEquals(5, carrinho.obterQuantidadeTotalDeItens());
	}

	@Test
	void obterNumeroDeItensRetornaQuantidadeDeProdutosDiferentes() {
		CarrinhoCompras carrinho = new CarrinhoCompras();
		carrinho.adicionarItem(novoItem(1, "Hamburguer", "12.50", 4));
		carrinho.adicionarItem(novoItem(2, "Batata", "6.00", 3));

		assertEquals(2, carrinho.obterNumeroDeItens());
	}

	@Test
	void contemItemIdentificaProdutoPeloIdentificador() {
		CarrinhoCompras carrinho = new CarrinhoCompras();
		carrinho.adicionarItem(novoItem(1, "Hamburguer", "12.50", 1));

		assertTrue(carrinho.contemItem(novoItem(1, "Outro nome", "1.00", 1)));
		assertFalse(carrinho.contemItem(novoItem(2, "Batata", "6.00", 1)));
	}

	@Test
	void contemItemNuloLancaExcecao() {
		CarrinhoCompras carrinho = new CarrinhoCompras();

		assertThrows(IllegalArgumentException.class, () -> carrinho.contemItem(null));
	}

	@Test
	void contemProdutoInformaSeIdentificadorEstaNoCarrinho() {
		CarrinhoCompras carrinho = new CarrinhoCompras();
		carrinho.adicionarItem(novoItem(1, "Hamburguer", "12.50", 1));

		assertTrue(carrinho.contemProduto(1L));
		assertFalse(carrinho.contemProduto(2L));
	}

	@Test
	void buscarItemPorProdutoIdRetornaItemEncontrado() {
		CarrinhoCompras carrinho = new CarrinhoCompras();
		ItemCarrinho item = novoItem(1, "Hamburguer", "12.50", 1);
		carrinho.adicionarItem(item);

		assertEquals(item, carrinho.buscarItemPorProdutoId(1L).orElseThrow());
	}

	@Test
	void buscarItemPorProdutoIdRetornaVazioQuandoNaoEncontrado() {
		CarrinhoCompras carrinho = new CarrinhoCompras();

		assertTrue(carrinho.buscarItemPorProdutoId(1L).isEmpty());
	}

	@Test
	void buscarItemPorProdutoIdRejeitaIdentificadorInvalido() {
		CarrinhoCompras carrinho = new CarrinhoCompras();

		assertThrows(IllegalArgumentException.class, () -> carrinho.buscarItemPorProdutoId(null));
		assertThrows(IllegalArgumentException.class, () -> carrinho.buscarItemPorProdutoId(-1L));
	}

	@Test
	void calcularTotalSomaSubtotaisDosProdutos() {
		CarrinhoCompras carrinho = new CarrinhoCompras();
		carrinho.adicionarItem(novoItem(1, "Hamburguer", "12.50", 2));
		carrinho.adicionarItem(novoItem(2, "Batata", "6.00", 1));

		assertEquals(new BigDecimal("31.00"), carrinho.calcularTotal());
	}

	@Test
	void calcularTotalDoCarrinhoVazioRetornaZero() {
		CarrinhoCompras carrinho = new CarrinhoCompras();

		assertEquals(BigDecimal.ZERO, carrinho.calcularTotal());
	}

	@Test
	void gerarPedidoCriaPedidoComItensEValorTotal() {
		CarrinhoCompras carrinho = new CarrinhoCompras();
		carrinho.adicionarItem(novoItem(1, "Hamburguer", "12.50", 2));

		Pedido pedido = carrinho.gerarPedido(10L);

		assertEquals(10L, pedido.getClienteId());
		assertEquals(1, pedido.getItens().size());
		assertEquals(new BigDecimal("25.00"), pedido.getTotal());
		assertNotSame(carrinho.listarItens().get(0), pedido.getItens().get(0));
	}

	@Test
	void gerarPedidoComCarrinhoVazioLancaExcecao() {
		CarrinhoCompras carrinho = new CarrinhoCompras();

		assertThrows(IllegalArgumentException.class, () -> carrinho.gerarPedido(10L));
	}

	@Test
	void estaVazioRefleteEstadoDoCarrinho() {
		CarrinhoCompras carrinho = new CarrinhoCompras();
		assertTrue(carrinho.estaVazio());

		carrinho.adicionarItem(novoItem(1, "Hamburguer", "12.50", 1));

		assertFalse(carrinho.estaVazio());
	}

	@Test
	void limparCarrinhoRemoveTodosOsItens() {
		CarrinhoCompras carrinho = new CarrinhoCompras();
		carrinho.adicionarItem(novoItem(1, "Hamburguer", "12.50", 2));
		carrinho.adicionarItem(novoItem(2, "Batata", "6.00", 1));

		carrinho.limparCarrinho();

		assertTrue(carrinho.estaVazio());
		assertEquals(BigDecimal.ZERO, carrinho.calcularTotal());
	}

	private ItemCarrinho novoItem(long produtoId, String nome, String preco, int quantidade) {
		Produto produto = new Produto(nome, "Produto de teste", new BigDecimal(preco), new Categoria("Lanches"));
		produto.setId(produtoId);
		return new ItemCarrinho(produto, quantidade);
	}
}
