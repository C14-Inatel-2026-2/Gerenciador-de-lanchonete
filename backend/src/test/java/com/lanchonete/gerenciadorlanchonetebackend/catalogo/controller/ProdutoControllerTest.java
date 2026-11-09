package com.lanchonete.gerenciadorlanchonetebackend.catalogo.controller;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Categoria;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.model.Produto;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.repository.CategoriaRepository;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.repository.ProdutoRepository;
import com.lanchonete.gerenciadorlanchonetebackend.catalogo.service.ProdutoService;
import com.lanchonete.gerenciadorlanchonetebackend.config.GlobalExceptionHandler;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;
import com.lanchonete.gerenciadorlanchonetebackend.user.security.UsuarioAtualProvider;
import com.lanchonete.gerenciadorlanchonetebackend.user.service.AuthorizationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(MockitoExtension.class)
class ProdutoControllerTest {
    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private UsuarioAtualProvider usuarioAtualProvider;

    private MockMvc mockMvc;

    @BeforeEach
    void configurarMockMvc() {
        ProdutoService produtoService = new ProdutoService(produtoRepository, categoriaRepository);
        ProdutoController controller = new ProdutoController(
                produtoService,
                usuarioAtualProvider,
                new AuthorizationService()
        );
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
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
    void deveListarSomenteProdutosDisponiveis() throws Exception {
        Categoria categoria = new Categoria("Lanches");
        categoria.setId(1L);
        Produto produto = new Produto("X-Burger", "Pão, carne e queijo", BigDecimal.valueOf(25), categoria);
        when(produtoRepository.findByDisponivelTrue()).thenReturn(List.of(produto));

        mockMvc.perform(get("/api/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("X-Burger"))
                .andExpect(jsonPath("$[0].disponivel").value(true))
                .andExpect(jsonPath("$[1]").doesNotExist());

        verify(produtoRepository).findByDisponivelTrue();
    }

    @Test
    void deveFiltrarProdutosDisponiveisPorCategoria() throws Exception {
        Categoria categoria = new Categoria("Lanches");
        categoria.setId(1L);
        Produto produto = new Produto("X-Burger", "Pão, carne e queijo", BigDecimal.valueOf(25), categoria);
        when(produtoRepository.findByCategoriaIdAndDisponivelTrue(1L)).thenReturn(List.of(produto));

        mockMvc.perform(get("/api/produtos").param("categoriaId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("X-Burger"))
                .andExpect(jsonPath("$[0].categoria.id").value(1))
                .andExpect(jsonPath("$[1]").doesNotExist());

        verify(produtoRepository).findByCategoriaIdAndDisponivelTrue(1L);
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

    @Test
    void deveCriarProdutoQuandoUsuarioForAdmin() throws Exception {
        autorizarAdmin();
        Categoria categoria = categoriaAtiva();
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(produtoRepository.save(any(Produto.class))).thenAnswer(invocation -> {
            Produto produto = invocation.getArgument(0);
            produto.setId(2L);
            return produto;
        });

        mockMvc.perform(post("/api/produtos")
                        .contentType("application/json")
                        .content("""
                                {"nome":"X-Burger","descricao":"Delicioso","preco":25,"categoriaId":1}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.nome").value("X-Burger"))
                .andExpect(jsonPath("$.categoriaId").value(1))
                .andExpect(jsonPath("$.disponivel").value(true));

        verify(produtoRepository).save(any(Produto.class));
    }

    @Test
    void naoDeveCriarProdutoQuandoUsuarioNaoForAdmin() throws Exception {
        autorizarCliente();

        mockMvc.perform(post("/api/produtos")
                        .contentType("application/json")
                        .content("""
                                {"nome":"X-Burger","descricao":"Delicioso","preco":25,"categoriaId":1}
                                """))
                .andExpect(status().isForbidden());

        verifyNoInteractions(produtoRepository, categoriaRepository);
    }

    @Test
    void naoDeveCriarProdutoQuandoCategoriaNaoExistir() throws Exception {
        autorizarAdmin();
        when(categoriaRepository.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(post("/api/produtos")
                        .contentType("application/json")
                        .content("""
                                {"nome":"X-Burger","descricao":"Delicioso","preco":25,"categoriaId":99}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Categoria não encontrada"));

        verify(produtoRepository, never()).save(any(Produto.class));
    }

    @Test
    void naoDeveCriarProdutoComPrecoNegativo() throws Exception {
        autorizarAdmin();

        mockMvc.perform(post("/api/produtos")
                        .contentType("application/json")
                        .content("""
                                {"nome":"X-Burger","descricao":"Delicioso","preco":-1,"categoriaId":1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Preço do produto deve ser maior que zero"));

        verify(produtoRepository, never()).save(any(Produto.class));
    }

    @Test
    void deveEditarProdutoQuandoUsuarioForAdmin() throws Exception {
        autorizarAdmin();
        Categoria categoria = categoriaAtiva();
        Produto produto = new Produto("X-Burger", "Delicioso", BigDecimal.valueOf(25), categoria);
        produto.setId(2L);
        when(produtoRepository.findById(2L)).thenReturn(Optional.of(produto));
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(produtoRepository.save(produto)).thenReturn(produto);

        mockMvc.perform(put("/api/produtos/2")
                        .contentType("application/json")
                        .content("""
                                {"nome":"X-Burger","descricao":"Pão e carne","preco":27,"categoriaId":1}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("X-Burger"))
                .andExpect(jsonPath("$.descricao").value("Pão e carne"))
                .andExpect(jsonPath("$.preco").value(27));

        verify(produtoRepository).existsByNomeAndIdNot("X-Burger", 2L);
        verify(produtoRepository).save(produto);
    }

    @Test
    void deveDesativarProdutoQuandoUsuarioForAdmin() throws Exception {
        autorizarAdmin();
        Categoria categoria = categoriaAtiva();
        Produto produto = new Produto("X-Burger", "Delicioso", BigDecimal.valueOf(25), categoria);
        produto.setId(2L);
        when(produtoRepository.findById(2L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(produto)).thenReturn(produto);

        mockMvc.perform(patch("/api/produtos/2/desativar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.disponivel").value(false));

        verify(produtoRepository).save(produto);
    }

    @Test
    void naoDeveDesativarProdutoQuandoUsuarioNaoForAdmin() throws Exception {
        autorizarCliente();

        mockMvc.perform(patch("/api/produtos/2/desativar"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(produtoRepository, categoriaRepository);
    }

    private void autorizarAdmin() {
        when(usuarioAtualProvider.obterUsuarioAtual())
                .thenReturn(new UserProfile("admin-1", "Admin", "admin@example.com", UserRole.ADMIN));
    }

    private void autorizarCliente() {
        when(usuarioAtualProvider.obterUsuarioAtual())
                .thenReturn(new UserProfile("cliente-1", "Cliente", "cliente@example.com", UserRole.CLIENTE));
    }

    private Categoria categoriaAtiva() {
        Categoria categoria = new Categoria("Lanches");
        categoria.setId(1L);
        return categoria;
    }
}
