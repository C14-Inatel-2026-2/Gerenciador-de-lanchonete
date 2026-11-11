package com.lanchonete.gerenciadorlanchonetebackend.user.security;

import com.lanchonete.gerenciadorlanchonetebackend.exception.AcessoNegadoException;
import com.lanchonete.gerenciadorlanchonetebackend.exception.UsuarioNaoAutenticadoException;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.repository.UserProfileRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * Identifica o usuário pelo token JWT do Supabase (#55).
 *
 * O SecurityConfig já validou o token (assinatura, emissor e expiração) antes
 * de a requisição chegar aqui. Este provider só pega o "sub" (id do usuário no
 * Supabase) e busca o perfil no nosso banco. Quem é o usuário é decidido pelo
 * token, nunca por um id enviado no header ou no corpo.
 */
@Component
public class SupabaseJwtUsuarioAtualProvider implements UsuarioAtualProvider {

    static final String NAO_AUTENTICADO = "Usuário não autenticado";
    static final String SEM_PERFIL = "Perfil não encontrado. Conclua o cadastro.";

    private final UserProfileRepository userProfileRepository;

    public SupabaseJwtUsuarioAtualProvider(UserProfileRepository userProfileRepository) {
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public UserProfile obterUsuarioAtual() {
        UserProfile usuario = userProfileRepository.findBySupabaseUserId(idDoToken())
                .orElseThrow(() -> new UsuarioNaoAutenticadoException(SEM_PERFIL));
        if (!usuario.isAtivo()) {
            throw new AcessoNegadoException("Usuário desativado");
        }
        return usuario;
    }

    // Rotas públicas chegam aqui sem token (usuário anônimo): também é 401.
    private static String idDoToken() {
        Authentication autenticacao = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacao instanceof JwtAuthenticationToken token) {
            return token.getToken().getSubject();
        }
        throw new UsuarioNaoAutenticadoException(NAO_AUTENTICADO);
    }
}
