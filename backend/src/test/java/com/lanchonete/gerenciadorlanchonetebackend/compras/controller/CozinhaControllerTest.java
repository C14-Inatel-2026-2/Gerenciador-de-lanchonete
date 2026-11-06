package com.lanchonete.gerenciadorlanchonetebackend.compras.controller;

import com.lanchonete.gerenciadorlanchonetebackend.compras.model.ItemPedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.Pedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.service.PedidoLifecycleService;
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
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Teste de controller: sobe só a camada web (controller + GlobalExceptionHandler),
// sem banco. O service e o usuário logado são mocks; a regra de permissão é a real.
@WebMvcTest(CozinhaController.class)
@Import(AuthorizationService.class)
class CozinhaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PedidoLifecycleService lifecycleService;

    @MockBean
    private UsuarioAtualProvider usuarioAtualProvider;

    @Test
    void filaRetornaPedidosNoFormatoDoContratoParaFuncionario() throws Exception {
        logarComo(UserRole.FUNCIONARIO);
        when(lifecycleService.listarFila()).thenReturn(List.of(pedido(5L), pedido(6L)));

        mockMvc.perform(get("/api/pedidos/fila"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(5))
            .andExpect(jsonPath("$[0].clienteId").value(1))
            .andExpect(jsonPath("$[0].status").value("CRIADO"))
            .andExpect(jsonPath("$[0].total").value(25.0))
            .andExpect(jsonPath("$[0].criadoEm").exists())
            .andExpect(jsonPath("$[0].itens[0].nome").value("Hamburguer"))
            .andExpect(jsonPath("$[0].itens[0].subtotal").value(25.0))
            .andExpect(jsonPath("$[1].id").value(6));
    }

    @Test
    void filaPermitidaParaAdmin() throws Exception {
        logarComo(UserRole.ADMIN);
        when(lifecycleService.listarFila()).thenReturn(List.of());

        mockMvc.perform(get("/api/pedidos/fila"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void filaNegadaParaClienteSemConsultarPedidos() throws Exception {
        logarComo(UserRole.CLIENTE);

        mockMvc.perform(get("/api/pedidos/fila"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.erro").value("Acesso restrito a funcionários e administradores"));

        verifyNoInteractions(lifecycleService);
    }

    @Test
    void filaSemUsuarioIdentificadoRetorna401() throws Exception {
        when(usuarioAtualProvider.obterUsuarioAtual())
            .thenThrow(new UsuarioNaoAutenticadoException("Usuário não autenticado"));

        mockMvc.perform(get("/api/pedidos/fila"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.erro").value("Usuário não autenticado"));

        verifyNoInteractions(lifecycleService);
    }

    private void logarComo(UserRole role) {
        UserProfile usuario = new UserProfile("teste-" + role, "Usuário Teste", "teste@lanchonete.com", role);
        when(usuarioAtualProvider.obterUsuarioAtual()).thenReturn(usuario);
    }

    private Pedido pedido(Long id) {
        Pedido pedido = new Pedido(1L, List.of(new ItemPedido(10L, "Hamburguer", new BigDecimal("12.50"), 2)));
        pedido.setId(id);
        return pedido;
    }
}