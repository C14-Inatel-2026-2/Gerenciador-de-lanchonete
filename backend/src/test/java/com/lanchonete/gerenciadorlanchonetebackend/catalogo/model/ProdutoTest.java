package com.lanchonete.gerenciadorlanchonetebackend.catalogo.model;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.exception.EstoqueInsuficienteException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ProdutoTest {

    @Test
    void deveCriarProdutoComValoresCorretos(){
        Categoria categoria = new Categoria("Lanches");
        Produto produto = new Produto("X-Burguer", "Delicioso", BigDecimal.valueOf(15), categoria);

        assertEquals("X-Burguer", produto.getNome());
        assertEquals(BigDecimal.valueOf(15), produto.getPreco());
        assertTrue(produto.isDisponivel());
        assertEquals(categoria, produto.getCategoria());
    }

    @Test
    void deveMarcarProdutoComoIndisponivel(){
        Produto produto = new Produto("Suco Natural", "Laranja", BigDecimal.valueOf(8), new Categoria("Bebidas"));

        produto.setDisponivel(false);

        assertFalse(produto.isDisponivel());
    }

    @Test
    void deveBaixarEstoqueAteZero() {
        Produto produto = new Produto("Suco", "Laranja", BigDecimal.valueOf(8), new Categoria("Bebidas"));
        produto.setQuantidadeEstoque(3);

        produto.baixarEstoque(3);

        assertEquals(0, produto.getQuantidadeEstoque());
        assertTrue(produto.isEsgotado());
    }

    @Test
    void naoDeveBaixarMaisDoQueOEstoqueDisponivel() {
        Produto produto = new Produto("Suco", "Laranja", BigDecimal.valueOf(8), new Categoria("Bebidas"));
        produto.setQuantidadeEstoque(2);

        assertThrows(EstoqueInsuficienteException.class, () -> produto.baixarEstoque(3));

        assertEquals(2, produto.getQuantidadeEstoque());
    }

    @Test
    void deveReporEstoque() {
        Produto produto = new Produto("Suco", "Laranja", BigDecimal.valueOf(8), new Categoria("Bebidas"));
        produto.setQuantidadeEstoque(2);

        produto.reporEstoque(4);

        assertEquals(6, produto.getQuantidadeEstoque());
    }

    @Test
    void naoDevePermitirEstoqueNegativo() {
        Produto produto = new Produto("Suco", "Laranja", BigDecimal.valueOf(8), new Categoria("Bebidas"));

        assertThrows(IllegalArgumentException.class, () -> produto.setQuantidadeEstoque(-1));
        assertEquals(0, produto.getQuantidadeEstoque());
    }
}
