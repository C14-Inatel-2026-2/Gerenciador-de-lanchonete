package com.lanchonete.gerenciadorlanchonetebackend.user.security;

import com.lanchonete.gerenciadorlanchonetebackend.exception.AcessoNegadoException;
import com.lanchonete.gerenciadorlanchonetebackend.exception.UsuarioNaoAutenticadoException;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.repository.UserProfileRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Implementação PROVISÓRIA: identifica o usuário pelo header X-Usuario-Id
 * (id do UserProfile). Será substituída pelo SupabaseJwtUsuarioAtualProvider na #55.
 */
@Component
public class HeaderUsuarioAtualProvider implements UsuarioAtualProvider {

    public static final String HEADER_USUARIO_ID = "X-Usuario-Id";

    private static final String NAO_AUTENTICADO = "Usuário não autenticado";

    private final UserProfileRepository userProfileRepository;
    // O Spring injeta um proxy que aponta sempre para a requisição atual.
    private final HttpServletRequest request;

    public HeaderUsuarioAtualProvider(UserProfileRepository userProfileRepository, HttpServletRequest request) {
        this.userProfileRepository = userProfileRepository;
        this.request = request;
    }

    @Override
    public UserProfile obterUsuarioAtual() {
        UserProfile usuario = userProfileRepository.findById(lerIdDoHeader())
                .orElseThrow(() -> new UsuarioNaoAutenticadoException(NAO_AUTENTICADO));
        if (!usuario.isAtivo()) {
            throw new AcessoNegadoException("Usuário desativado");
        }
        return usuario;
    }

    private Long lerIdDoHeader() {
        String valor = request.getHeader(HEADER_USUARIO_ID);
        if (valor == null || valor.isBlank()) {
            throw new UsuarioNaoAutenticadoException(NAO_AUTENTICADO);
        }
        try {
            return Long.valueOf(valor.trim());
        } catch (NumberFormatException e) {
            throw new UsuarioNaoAutenticadoException(NAO_AUTENTICADO);
        }
    }
}
