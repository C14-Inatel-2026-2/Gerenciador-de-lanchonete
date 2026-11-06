package com.lanchonete.gerenciadorlanchonetebackend.compras.controller;

import com.lanchonete.gerenciadorlanchonetebackend.compras.dto.AtualizarStatusRequest;
import com.lanchonete.gerenciadorlanchonetebackend.compras.dto.PedidoResponse;
import com.lanchonete.gerenciadorlanchonetebackend.compras.service.PedidoLifecycleService;
import com.lanchonete.gerenciadorlanchonetebackend.exception.AcessoNegadoException;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.security.UsuarioAtualProvider;
import com.lanchonete.gerenciadorlanchonetebackend.user.service.AuthorizationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Rotas da cozinha (US04, docs/api.md). Fica separado das rotas de pedido do
 * cliente (US03) para cada módulo ter o seu controller, sem conflito de arquivo.
 */
@RestController
@RequestMapping("/api/pedidos")
public class CozinhaController {

    private final PedidoLifecycleService lifecycleService;
    private final UsuarioAtualProvider usuarioAtualProvider;
    private final AuthorizationService authorizationService;

    public CozinhaController(
            PedidoLifecycleService lifecycleService,
            UsuarioAtualProvider usuarioAtualProvider,
            AuthorizationService authorizationService
    ) {
        this.lifecycleService = lifecycleService;
        this.usuarioAtualProvider = usuarioAtualProvider;
        this.authorizationService = authorizationService;
    }

    @GetMapping("/fila")
    public List<PedidoResponse> listarFila() {
        exigirAcessoCozinha();
        return lifecycleService.listarFila().stream()
            .map(PedidoResponse::de)
            .toList();
    }


    // A regra da transição não fica aqui: o service chama Pedido.alterarStatus,
    // e uma transição inválida vira 409 no GlobalExceptionHandler (RNF-CIC05).
    @PatchMapping("/{id}/status")
    public PedidoResponse atualizarStatus(@PathVariable Long id, @RequestBody AtualizarStatusRequest request) {
        exigirAcessoCozinha();
        if (request.status() == null) {
            throw new IllegalArgumentException("O novo status não pode ser nulo.");
        }
        return PedidoResponse.de(lifecycleService.atualizarStatusPorId(id, request.status()));
    }

    // 401 sai do provider (usuário não identificado); 403 sai daqui (sem permissão).
    private void exigirAcessoCozinha() {
        UserProfile usuario = usuarioAtualProvider.obterUsuarioAtual();
        if (!authorizationService.podeOperarCozinha(usuario)) {
            throw new AcessoNegadoException("Acesso restrito a funcionários e administradores");
        }
    }
}