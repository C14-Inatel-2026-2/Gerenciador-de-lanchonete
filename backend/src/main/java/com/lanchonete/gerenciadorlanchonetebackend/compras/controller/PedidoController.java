package com.lanchonete.gerenciadorlanchonetebackend.compras.controller;

import com.lanchonete.gerenciadorlanchonetebackend.compras.dto.PedidoRequest;
import com.lanchonete.gerenciadorlanchonetebackend.compras.dto.PedidoResponse;
import com.lanchonete.gerenciadorlanchonetebackend.compras.model.Pedido;
import com.lanchonete.gerenciadorlanchonetebackend.compras.service.PedidoService;
import com.lanchonete.gerenciadorlanchonetebackend.exception.AcessoNegadoException;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.security.UsuarioAtualProvider;
import com.lanchonete.gerenciadorlanchonetebackend.user.service.AuthorizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    private final PedidoService pedidoService;
    private final UsuarioAtualProvider usuarioAtualProvider;
    private final AuthorizationService authorizationService;

    public PedidoController(
            PedidoService pedidoService,
            UsuarioAtualProvider usuarioAtualProvider,
            AuthorizationService authorizationService
    ) {
        this.pedidoService = pedidoService;
        this.usuarioAtualProvider = usuarioAtualProvider;
        this.authorizationService = authorizationService;
    }

    @PostMapping
    public ResponseEntity<PedidoResponse> criarPedido(@RequestBody PedidoRequest request) {
        UserProfile cliente = exigirCliente();
        Pedido pedido = pedidoService.criarPedidoComItens(cliente.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PedidoResponse.de(pedido));
    }

    @GetMapping("/meus")
    public List<PedidoResponse> listarMeusPedidos() {
        UserProfile cliente = exigirCliente();
        return pedidoService.listarPedidosDoCliente(cliente.getId()).stream()
                .map(PedidoResponse::de)
                .toList();
    }

    private UserProfile exigirCliente() {
        UserProfile usuario = usuarioAtualProvider.obterUsuarioAtual();
        if (!authorizationService.podeRealizarPedidos(usuario)) {
            throw new AcessoNegadoException("Apenas clientes podem criar e consultar pedidos próprios");
        }
        return usuario;
    }
}
