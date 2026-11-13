package com.lanchonete.gerenciadorlanchonetebackend.catalogo.service;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto.CardapioResponse;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto.CategoriaCardapioResponse;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto.ProdutoCardapioResponse;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.exception.ProdutoDuplicadoException;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Categoria;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Produto;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.repository.CategoriaRepository;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.repository.ProdutoRepository;
import com.lanchonete.gerenciadorlanchonetebackend.exception.RecursoNaoEncontradoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProdutoServiceTest {
    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private ProdutoService produtoService;

    private Categoria categoria;

    @BeforeEach
    void setUp() {
        categoria = new Categoria("Lanches");
        categoria.setId(1L);
    }

    @Test
    void excecaoParaPrecoNegativo() {
        Produto produto = produtoComPreco(BigDecimal.valueOf(-10));

        assertThrows(IllegalArgumentException.class, () -> produtoService.salvar(produto));
        verify(produtoRepository, never()).save(any());
    }

    @Test
    void excecaoParaProdutoSemCategoria() {
        Produto produto = new Produto("X-Burguer", "Delicioso", BigDecimal.valueOf(15), null);

        assertThrows(IllegalArgumentException.class, () -> produtoService.salvar(produto));
    }

    @Test
    void deveSalvarProdutoValido() {
        Produto produto = produtoComPreco(BigDecimal.valueOf(15));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(produtoRepository.save(produto)).thenReturn(produto);

        Produto salvo = produtoService.salvar(produto);

        assertEquals(produto, salvo);
        verify(produtoRepository).save(produto);
    }

    @Test
    void excecaoParaProdutoComNomeDuplicado(){
        Produto produto = new Produto(" x-burguer ", "Delicioso", BigDecimal.valueOf(15), categoria);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(produtoRepository.existsByNomeIgnoreCase("x-burguer")).thenReturn(true);

        assertThrows(ProdutoDuplicadoException.class, () -> produtoService.salvar(produto));

        verify(produtoRepository, never()).save(any());
    }

    @Test
    void deveApararNomeDoProdutoAoSalvar() {
        Produto produto = new Produto(" X-Burger ", "Delicioso", BigDecimal.valueOf(15), categoria);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(produtoRepository.save(produto)).thenReturn(produto);

        Produto salvo = produtoService.salvar(produto);

        assertEquals("X-Burger", salvo.getNome());
        verify(produtoRepository).existsByNomeIgnoreCase("X-Burger");
        verify(produtoRepository).save(produto);
    }

    @Test
    void deveEditarProdutoSemTratarSeuProprioNomeComoDuplicado() {
        Produto produto = produtoComPreco(BigDecimal.valueOf(15));
        produto.setId(2L);
        when(produtoRepository.findById(2L)).thenReturn(Optional.of(produto));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(produtoRepository.save(produto)).thenReturn(produto);

        Produto editado = produtoService.editar(2L, "X-Burger", "Atualizado", BigDecimal.valueOf(16), 1L);

        assertEquals("Atualizado", editado.getDescricao());
        assertEquals(BigDecimal.valueOf(16), editado.getPreco());
        verify(produtoRepository).existsByNomeIgnoreCaseAndIdNot("X-Burger", 2L);
        verify(produtoRepository).save(produto);
    }

    @Test
    void deveApararNomeDoProdutoAoEditar() {
        Produto produto = produtoComPreco(BigDecimal.valueOf(15));
        produto.setId(2L);
        when(produtoRepository.findById(2L)).thenReturn(Optional.of(produto));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(produtoRepository.save(produto)).thenReturn(produto);

        Produto editado = produtoService.editar(2L, " X-Burger ", "Atualizado", BigDecimal.valueOf(16), 1L);

        assertEquals("X-Burger", editado.getNome());
        verify(produtoRepository).existsByNomeIgnoreCaseAndIdNot("X-Burger", 2L);
        verify(produtoRepository).save(produto);
    }

    @Test
    void naoDeveEditarProdutoComNomeDuplicadoIgnorandoMaiusculasMinusculas() {
        Produto produto = produtoComPreco(BigDecimal.valueOf(15));
        produto.setId(2L);
        when(produtoRepository.findById(2L)).thenReturn(Optional.of(produto));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(produtoRepository.existsByNomeIgnoreCaseAndIdNot("x-burger", 2L)).thenReturn(true);

        assertThrows(
                ProdutoDuplicadoException.class,
                () -> produtoService.editar(2L, " x-burger ", "Atualizado", BigDecimal.valueOf(16), 1L)
        );

        verify(produtoRepository).existsByNomeIgnoreCaseAndIdNot("x-burger", 2L);
        verify(produtoRepository, never()).save(any());
    }

    @Test
    void deveLancar404QuandoCategoriaNaoExistir() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.salvar(
                        produtoComPreco(BigDecimal.valueOf(15))
                )
        );

        verify(produtoRepository, never()).save(any());
    }

    @Test
    void deveAgruparProdutosDisponiveisPorCategoriaNoCardapio() {
        Categoria lanches = new Categoria("Lanches");
        lanches.setId(1L);
        Categoria bebidas = new Categoria("Bebidas");
        bebidas.setId(2L);

        Produto xBurguer = new Produto("X-Burger", "Pão, carne e queijo", BigDecimal.valueOf(25), lanches);
        xBurguer.setId(10L);
        xBurguer.setQuantidadeEstoque(4);
        Produto suco = new Produto("Suco", "Suco natural", BigDecimal.valueOf(8), bebidas);
        suco.setId(11L);
        suco.setQuantidadeEstoque(0);
        when(produtoRepository.findByDisponivelTrue()).thenReturn(List.of(xBurguer, suco));

        CardapioResponse cardapio = produtoService.obterCardapio();

        assertEquals(2, cardapio.categorias().size());
        assertEquals(1L, cardapio.categorias().get(0).id());
        assertEquals("Lanches", cardapio.categorias().get(0).nome());
        assertEquals(
                List.of(new ProdutoCardapioResponse(
                        10L,
                        "X-Burger",
                        "Pão, carne e queijo",
                        BigDecimal.valueOf(25),
                        4,
                        false
                )),
                cardapio.categorias().get(0).produtos()
        );
        assertEquals(2L, cardapio.categorias().get(1).id());
        assertEquals("Bebidas", cardapio.categorias().get(1).nome());
        assertEquals(
                List.of(new ProdutoCardapioResponse(
                        11L,
                        "Suco",
                        "Suco natural",
                        BigDecimal.valueOf(8),
                        0,
                        true
                )),
                cardapio.categorias().get(1).produtos()
        );
        assertNull(cardapio.mensagem());
    }

    @Test
    void deveInformarQuandoCardapioNaoTemProdutosDisponiveis() {
        when(produtoRepository.findByDisponivelTrue()).thenReturn(List.of());

        CardapioResponse cardapio = produtoService.obterCardapio();

        assertEquals(List.of(), cardapio.categorias());
        assertEquals("Cardápio vazio: não há produtos disponíveis.", cardapio.mensagem());
    }

    @Test
    void deveBaixarEstoqueDoProduto() {
        Produto produto = produtoComPreco(BigDecimal.valueOf(15));
        produto.setId(2L);
        produto.setQuantidadeEstoque(5);
        when(produtoRepository.findById(2L)).thenReturn(Optional.of(produto));
        when(produtoRepository.saveAndFlush(produto)).thenReturn(produto);

        Produto atualizado = produtoService.baixarEstoque(2L, 2);

        assertEquals(3, atualizado.getQuantidadeEstoque());
        verify(produtoRepository).saveAndFlush(produto);
    }

    @Test
    void deveReporEstoqueDoProduto() {
        Produto produto = produtoComPreco(BigDecimal.valueOf(15));
        produto.setId(2L);
        produto.setQuantidadeEstoque(5);
        when(produtoRepository.findById(2L)).thenReturn(Optional.of(produto));
        when(produtoRepository.saveAndFlush(produto)).thenReturn(produto);

        Produto atualizado = produtoService.reporEstoque(2L, 2);

        assertEquals(7, atualizado.getQuantidadeEstoque());
        verify(produtoRepository).saveAndFlush(produto);
    }

    @Test
    void deveRetornar404AoBaixarEstoqueDeProdutoInexistente() {
        when(produtoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> produtoService.baixarEstoque(99L, 1));
        verify(produtoRepository, never()).saveAndFlush(any());
    }

    private Produto produtoComPreco(BigDecimal preco) {
        return new Produto("X-Burger", "Delicioso", preco, categoria);
    }
}