package com.lanchonete.gerenciadorlanchonetebackend.catalogo.service;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto.CardapioResponse;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto.CategoriaCardapioResponse;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.dto.ProdutoCardapioResponse;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Categoria;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Produto;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.repository.CategoriaRepository;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.repository.ProdutoRepository;
import com.lanchonete.gerenciadorlanchonetebackend.exception.RecursoNaoEncontradoException;
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

    @Test
    void excecaoParaPrecoNegativo() {
        Categoria categoria = new Categoria("Lanches");
        Produto produto = new Produto("X-Burguer", "Delicioso", BigDecimal.valueOf(-10), categoria);

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
        Categoria categoria = new Categoria("Lanches");
        categoria.setId(1L);
        Produto produto = new Produto("X-Burger", "Delicioso", BigDecimal.valueOf(15), categoria);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(produtoRepository.save(produto)).thenReturn(produto);

        Produto salvo = produtoService.salvar(produto);

        assertEquals(produto, salvo);
        verify(produtoRepository).save(produto);
    }

    @Test
    void excecaoParaProdutoComNomeDuplicado(){
        Categoria categoria = new Categoria("Lanches");
        categoria.setId(1L);
        Produto produto = new Produto("X-Burguer", "Delicioso", BigDecimal.valueOf(15), categoria);

        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(produtoRepository.existsByNome("X-Burguer")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> produtoService.salvar(produto));

        verify(produtoRepository, never()).save(any());
    }

    @Test
    void deveEditarProdutoSemTratarSeuProprioNomeComoDuplicado() {
        Categoria categoria = new Categoria("Lanches");
        categoria.setId(1L);
        Produto produto = new Produto("X-Burger", "Delicioso", BigDecimal.valueOf(15), categoria);
        produto.setId(2L);
        when(produtoRepository.findById(2L)).thenReturn(Optional.of(produto));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(produtoRepository.save(produto)).thenReturn(produto);

        Produto editado = produtoService.editar(2L, "X-Burger", "Atualizado", BigDecimal.valueOf(16), 1L);

        assertEquals("Atualizado", editado.getDescricao());
        assertEquals(BigDecimal.valueOf(16), editado.getPreco());
        verify(produtoRepository).existsByNomeAndIdNot("X-Burger", 2L);
        verify(produtoRepository).save(produto);
    }

    @Test
    void deveLancar404QuandoCategoriaNaoExistir() {
        when(categoriaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                RecursoNaoEncontradoException.class,
                () -> produtoService.salvar(
                        new Produto("X-Burger", "Delicioso", BigDecimal.valueOf(15), categoriaComId(1L))
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
        Produto suco = new Produto("Suco", "Suco natural", BigDecimal.valueOf(8), bebidas);
        when(produtoRepository.findByDisponivelTrue()).thenReturn(List.of(xBurguer, suco));

        CardapioResponse cardapio = produtoService.obterCardapio();

        assertEquals(2, cardapio.categorias().size());
        assertEquals("Lanches", cardapio.categorias().get(0).nome());
        assertEquals(
                List.of(new ProdutoCardapioResponse("X-Burger", "Pão, carne e queijo", BigDecimal.valueOf(25))),
                cardapio.categorias().get(0).produtos()
        );
        assertEquals("Bebidas", cardapio.categorias().get(1).nome());
        assertEquals(
                List.of(new ProdutoCardapioResponse("Suco", "Suco natural", BigDecimal.valueOf(8))),
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

    private Categoria categoriaComId(Long id) {
        Categoria categoria = new Categoria("Lanches");
        categoria.setId(id);
        return categoria;
    }
}