package com.lanchonete.gerenciadorlanchonetebackend.compras.controller;

import com.lanchonete.gerenciadorlanchonetebackend.catalogo.exception.EstoqueInsuficienteException;
import com.lanchonete.gerenciadorlanchonetebackend.compras.dto.ItemPedidoRequest;
import com.lanchonete.gerenciadorlanchonetebackend.compras.dto.PedidoRequest;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.ItemPedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.Pedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.service.PedidoService;
import com.lanchonete.gerenciadorlanchonetebackend.exception.RecursoNaoEncontradoException;
import com.lanchonete.gerenciadorlanchonetebackend.exception.UsuarioNaoAutenticadoException;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;
import com.lanchonete.gerenciadorlanchonetebackend.user.security.UsuarioAtualProvider;
import com.lanchonete.gerenciadorlanchonetebackend.user.service.AuthorizationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PedidoController.class)
@Import(AuthorizationService.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoService pedidoService;

    @MockBean
    private UsuarioAtualProvider usuarioAtualProvider;

    @Test
    void postCriaPedidoParaClienteLogadoERetorna201() throws Exception {
        logarComoCliente(42L);
        when(pedidoService.criarPedidoComItens(eq(42L), any(PedidoRequest.class)))
                .thenReturn(pedido(7L));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"itens":[{"produtoId":10,"quantidade":2}]}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.clienteId").value(42))
                .andExpect(jsonPath("$.status").value("CRIADO"))
                .andExpect(jsonPath("$.total").value(37.8))
                .andExpect(jsonPath("$.itens[0].produtoId").value(10))
                .andExpect(jsonPath("$.itens[0].precoUnitario").value(18.9))
                .andExpect(jsonPath("$.itens[0].quantidade").value(2))
                .andExpect(jsonPath("$.itens[0].subtotal").value(37.8));

        verify(pedidoService).criarPedidoComItens(
                42L,
                new PedidoRequest(List.of(new ItemPedidoRequest(10L, 2)))
        );
    }

    @Test
    void getMeusRetornaSomentePedidosDoClienteLogado() throws Exception {
        logarComoCliente(42L);
        when(pedidoService.listarPedidosDoCliente(42L)).thenReturn(List.of(pedido(7L)));

        mockMvc.perform(get("/api/pedidos/meus"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(7))
                .andExpect(jsonPath("$[0].clienteId").value(42))
                .andExpect(jsonPath("$[0].itens[0].nome").value("X-Burguer"));

        verify(pedidoService).listarPedidosDoCliente(42L);
    }

    @Test
    void postComProdutoInexistenteRetorna404() throws Exception {
        logarComoCliente(42L);
        when(pedidoService.criarPedidoComItens(eq(42L), any(PedidoRequest.class)))
                .thenThrow(new RecursoNaoEncontradoException("Produto não encontrado"));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"itens":[{"produtoId":99,"quantidade":1}]}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.erro").value("Produto não encontrado"));
    }

    @Test
    void postComProdutoIndisponivelRetorna400() throws Exception {
        logarComoCliente(42L);
        when(pedidoService.criarPedidoComItens(eq(42L), any(PedidoRequest.class)))
                .thenThrow(new IllegalArgumentException("O produto deve estar disponível."));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"itens":[{"produtoId":10,"quantidade":1}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("O produto deve estar disponível."));
    }

    @Test
    void postComEstoqueInsuficienteRetorna409ComNomeDoProduto() throws Exception {
        logarComoCliente(42L);
        when(pedidoService.criarPedidoComItens(eq(42L), any(PedidoRequest.class)))
                .thenThrow(new EstoqueInsuficienteException("Estoque insuficiente para o produto X-Burger"));

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"itens":[{"produtoId":10,"quantidade":2}]}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.erro").value("Estoque insuficiente para o produto X-Burger"));
    }

    @Test
    void pedidoNegadoParaFuncionarioSemChamarService() throws Exception {
        logarComo(UserRole.FUNCIONARIO, 2L);

        mockMvc.perform(get("/api/pedidos/meus"))
                .andExpect(status().isForbidden());

        verifyNoInteractions(pedidoService);
    }

    @Test
    void pedidoSemUsuarioIdentificadoRetorna401() throws Exception {
        when(usuarioAtualProvider.obterUsuarioAtual())
                .thenThrow(new UsuarioNaoAutenticadoException("Usuário não autenticado"));

        mockMvc.perform(get("/api/pedidos/meus"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.erro").value("Usuário não autenticado"));

        verifyNoInteractions(pedidoService);
    }

    private void logarComoCliente(Long id) {
        logarComo(UserRole.CLIENTE, id);
    }

    private void logarComo(UserRole role, Long id) {
        UserProfile usuario = mock(UserProfile.class);
        when(usuario.getId()).thenReturn(id);
        when(usuario.getRole()).thenReturn(role);
        when(usuario.isAtivo()).thenReturn(true);
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(usuario);
    }

    private Pedido pedido(Long id) {
        Pedido pedido = new Pedido(42L, List.of(
                new ItemPedido(10L, "X-Burguer", new BigDecimal("18.90"), 2)
        ));
        pedido.setId(id);
        return pedido;
    }
}
