package com.lanchonete.gerenciadorlanchonetebackend.user.service;

import com.lanchonete.gerenciadorlanchonetebackend.exception.AcessoNegadoException;
import com.lanchonete.gerenciadorlanchonetebackend.exception.RecursoNaoEncontradoException;
import com.lanchonete.gerenciadorlanchonetebackend.user.exception.EmailJaCadastradoException;
import com.lanchonete.gerenciadorlanchonetebackend.user.exception.UserValidationException;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;
import com.lanchonete.gerenciadorlanchonetebackend.user.repository.UserProfileRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

@Service
public class UserService {

    private static final String SOMENTE_ADMIN = "Acesso restrito a administradores";

    private final UserProfileRepository userProfileRepository;
    private final AuthorizationService authorizationService;

    public UserService(
            UserProfileRepository userProfileRepository,
            AuthorizationService authorizationService
    ) {
        this.userProfileRepository = userProfileRepository;
        this.authorizationService = authorizationService;
    }

    /** Lista todos os perfis, ativos e desativados, em ordem de nome (RN-AUTHZ-01). */
    public List<UserProfile> listar(UserProfile solicitante) {
        if (!authorizationService.podeGerenciarUsuarios(solicitante)) {
            throw new AcessoNegadoException(SOMENTE_ADMIN);
        }
        return userProfileRepository.findAllByOrderByNomeAsc();
    }

    /** Altera a role de outro usuário (RN-AUTHZ-02). Ninguém altera a própria role (RN-AUTHZ-04). */
    public UserProfile alterarRole(UserProfile solicitante, Long usuarioId, UserRole novaRole) {
        if (!authorizationService.podeAlterarRole(solicitante)) {
            throw new AcessoNegadoException(SOMENTE_ADMIN);
        }
        if (novaRole == null) {
            throw new UserValidationException("Role é obrigatória");
        }
        if (solicitante.getId().equals(usuarioId)) {
            throw new AcessoNegadoException("Você não pode alterar a própria role");
        }
        UserProfile usuario = buscarPorId(usuarioId);
        usuario.alterarRole(novaRole);
        return userProfileRepository.save(usuario);
    }

    /**
     * Desativa um usuário (RN-AUTHZ-03). O perfil continua no banco, mas perde o
     * acesso (RN-USER-05). O admin não pode desativar a si mesmo, para não perder
     * o acesso ao painel.
     */
    public UserProfile desativar(UserProfile solicitante, Long usuarioId) {
        if (!authorizationService.podeDesativarUsuario(solicitante)) {
            throw new AcessoNegadoException(SOMENTE_ADMIN);
        }
        if (solicitante.getId().equals(usuarioId)) {
            throw new AcessoNegadoException("Você não pode desativar a si mesmo");
        }
        UserProfile usuario = buscarPorId(usuarioId);
        usuario.desativar();
        return userProfileRepository.save(usuario);
    }

    private UserProfile buscarPorId(Long usuarioId) {
        return userProfileRepository.findById(usuarioId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Usuário não encontrado"));
    }

    public UserProfile criarPerfil(
            String supabaseUserId,
            String nome,
            String email
    ) {

        validarSupabaseUserId(supabaseUserId);
        validarNome(nome);
        String emailNormalizado = validarEmail(email);

        if (userProfileRepository.existsByEmail(emailNormalizado)) {
            throw new EmailJaCadastradoException();
        }

        UserProfile userProfile = new UserProfile(
                supabaseUserId,
                nome,
                emailNormalizado,
                UserRole.CLIENTE
        );
        return userProfileRepository.save(userProfile);
    }

    private void validarSupabaseUserId(String supabaseUserId) {
        if (supabaseUserId == null || supabaseUserId.isBlank()) {
            throw new UserValidationException(
                    "Supabase User ID é obrigatório"
            );
        }
    }

    private void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new UserValidationException(
                    "Nome é obrigatório"
            );
        }
    }

    private String validarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new UserValidationException(
                    "E-mail é obrigatório"
            );
        }

        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
        if (!emailNormalizado.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new UserValidationException(
                    "E-mail inválido"
            );
        }
        return emailNormalizado;
    }
}