package com.lanchonete.gerenciadorlanchonetebackend.user.security;

import com.lanchonete.gerenciadorlanchonetebackend.exception.AcessoNegadoException;
import com.lanchonete.gerenciadorlanchonetebackend.exception.UsuarioNaoAutenticadoException;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;
import com.lanchonete.gerenciadorlanchonetebackend.user.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HeaderUsuarioAtualProviderTest {

    @Mock
    private UserProfileRepository repository;

    private MockHttpServletRequest request;
    private HeaderUsuarioAtualProvider provider;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        provider = new HeaderUsuarioAtualProvider(repository, request);
    }

    @Test
    void retornaUsuarioQuandoHeaderValidoEUsuarioAtivo() {
        UserProfile usuario = new UserProfile("supabase-1", "Karolina", "karolina@email.com", UserRole.CLIENTE);
        request.addHeader(HeaderUsuarioAtualProvider.HEADER_USUARIO_ID, "1");
        when(repository.findById(1L)).thenReturn(Optional.of(usuario));

        assertSame(usuario, provider.obterUsuarioAtual());
    }

    @Test
    void lanca401QuandoHeaderAusente() {
        assertThrows(UsuarioNaoAutenticadoException.class, () -> provider.obterUsuarioAtual());
        verify(repository, never()).findById(any());
    }

    @Test
    void lanca401QuandoHeaderNaoNumerico() {
        request.addHeader(HeaderUsuarioAtualProvider.HEADER_USUARIO_ID, "abc");

        assertThrows(UsuarioNaoAutenticadoException.class, () -> provider.obterUsuarioAtual());
        verify(repository, never()).findById(any());
    }

    @Test
    void lanca401QuandoUsuarioNaoExiste() {
        request.addHeader(HeaderUsuarioAtualProvider.HEADER_USUARIO_ID, "99");
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UsuarioNaoAutenticadoException.class, () -> provider.obterUsuarioAtual());
    }

    @Test
    void lanca403QuandoUsuarioDesativado() {
        UserProfile usuario = new UserProfile("supabase-2", "Ana", "ana@email.com", UserRole.FUNCIONARIO);
        usuario.desativar();
        request.addHeader(HeaderUsuarioAtualProvider.HEADER_USUARIO_ID, "2");
        when(repository.findById(2L)).thenReturn(Optional.of(usuario));

        assertThrows(AcessoNegadoException.class, () -> provider.obterUsuarioAtual());
    }
}
