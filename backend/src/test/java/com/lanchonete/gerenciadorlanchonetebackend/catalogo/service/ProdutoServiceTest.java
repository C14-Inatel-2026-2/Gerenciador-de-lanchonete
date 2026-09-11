package com.lanchonete.gerenciadorlanchonetebackend.catalogo.service;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Categoria;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Produto;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProdutoServiceTest {
    @Mock
    private ProdutoRepository produtoRepository;

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
        Produto produto = new Produto("X-Burger", "Delicioso", BigDecimal.valueOf(15), categoria);
        when(produtoRepository.save(produto)).thenReturn(produto);

        Produto salvo = produtoService.salvar(produto);

        assertEquals(produto, salvo);
        verify(produtoRepository).save(produto);
    }
}
