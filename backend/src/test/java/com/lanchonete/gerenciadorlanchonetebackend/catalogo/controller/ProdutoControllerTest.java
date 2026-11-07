package com.lanchonete.gerenciadorlanchonetebackend.catalogo.controller;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Categoria;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Produto;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.repository.ProdutoRepository;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.service.ProdutoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class ProdutoControllerTest {
    @Mock
    private ProdutoRepository produtoRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void configurarMockMvc() {
        ProdutoService produtoService = new ProdutoService(produtoRepository);
        mockMvc = standaloneSetup(new ProdutoController(produtoService)).build();
    }

    @Test
    void deveRetornarCardapioAgrupado() throws Exception {
        Categoria categoria = new Categoria("Lanches");
        categoria.setId(1L);
        Produto produto = new Produto("X-Burger", "Pão, carne e queijo", BigDecimal.valueOf(25), categoria);
        when(produtoRepository.findByDisponivelTrue()).thenReturn(List.of(produto));

        mockMvc.perform(get("/api/produtos/cardapio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorias[0].nome").value("Lanches"))
                .andExpect(jsonPath("$.categorias[0].produtos[0].nome").value("X-Burger"))
                .andExpect(jsonPath("$.categorias[0].produtos[0].descricao").value("Pão, carne e queijo"))
                .andExpect(jsonPath("$.categorias[0].produtos[0].preco").value(25));

        verify(produtoRepository).findByDisponivelTrue();
    }

    @Test
    void deveRetornarMensagemQuandoCardapioEstiverVazio() throws Exception {
        when(produtoRepository.findByDisponivelTrue()).thenReturn(List.of());

        mockMvc.perform(get("/api/produtos/cardapio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorias").isEmpty())
                .andExpect(jsonPath("$.mensagem").value("Cardápio vazio: não há produtos disponíveis."));

        verify(produtoRepository).findByDisponivelTrue();
    }
}
