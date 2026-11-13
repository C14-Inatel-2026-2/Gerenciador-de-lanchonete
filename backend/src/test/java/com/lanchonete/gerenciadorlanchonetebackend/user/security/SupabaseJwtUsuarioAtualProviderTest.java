package com.lanchonete.gerenciadorlanchonetebackend.user.security;

import com.lanchonete.gerenciadorlanchonetebackend.exception.AcessoNegadoException;
import com.lanchonete.gerenciadorlanchonetebackend.exception.UsuarioNaoAutenticadoException;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;
import com.lanchonete.gerenciadorlanchonetebackend.user.repository.UserProfileRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SupabaseJwtUsuarioAtualProviderTest {

    @Mock
    private UserProfileRepository repository;

    private SupabaseJwtUsuarioAtualProvider provider;

    @BeforeEach
    void setUp() {
        provider = new SupabaseJwtUsuarioAtualProvider(repository);
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    private static void logarComToken(String sub) {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "ES256")
                .subject(sub)
                .claim("email", "cliente@email.com")
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));
    }

    @Test
    void retornaPerfilDoSubDoToken() {
        UserProfile cliente = new UserProfile("sub-123", "Cliente", "cliente@email.com", UserRole.CLIENTE);
        logarComToken("sub-123");
        when(repository.findBySupabaseUserId("sub-123")).thenReturn(Optional.of(cliente));

        assertSame(cliente, provider.obterUsuarioAtual());
    }

    @Test
    void tokenSemPerfilNoBancoRetornaNaoAutenticado() {
        logarComToken("sub-sem-perfil");
        when(repository.findBySupabaseUserId("sub-sem-perfil")).thenReturn(Optional.empty());

        UsuarioNaoAutenticadoException ex =
                assertThrows(UsuarioNaoAutenticadoException.class, provider::obterUsuarioAtual);
        assertEquals("Perfil não encontrado. Conclua o cadastro.", ex.getMessage());
    }

    @Test
    void perfilDesativadoRetornaAcessoNegado() {
        UserProfile desativado = new UserProfile("sub-123", "Cliente", "cliente@email.com", UserRole.CLIENTE);
        desativado.desativar();
        logarComToken("sub-123");
        when(repository.findBySupabaseUserId("sub-123")).thenReturn(Optional.of(desativado));

        AcessoNegadoException ex = assertThrows(AcessoNegadoException.class, provider::obterUsuarioAtual);
        assertEquals("Usuário desativado", ex.getMessage());
    }

    @Test
    void semTokenRetornaNaoAutenticadoSemConsultarOBanco() {
        assertThrows(UsuarioNaoAutenticadoException.class, provider::obterUsuarioAtual);
        verify(repository, never()).findBySupabaseUserId(any());
    }

    @Test
    void usuarioAnonimoDeRotaPublicaRetornaNaoAutenticado() {
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "chave", "anonymousUser", AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS")));

        assertThrows(UsuarioNaoAutenticadoException.class, provider::obterUsuarioAtual);
        verify(repository, never()).findBySupabaseUserId(any());
    }
}
