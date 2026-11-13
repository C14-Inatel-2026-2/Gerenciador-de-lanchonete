package com.lanchonete.gerenciadorlanchonetebackend.user.service;

import com.lanchonete.gerenciadorlanchonetebackend.exception.AcessoNegadoException;
import com.lanchonete.gerenciadorlanchonetebackend.exception.RecursoNaoEncontradoException;
import com.lanchonete.gerenciadorlanchonetebackend.user.exception.EmailJaCadastradoException;
import com.lanchonete.gerenciadorlanchonetebackend.user.exception.UserValidationException;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserRole;
import com.lanchonete.gerenciadorlanchonetebackend.user.repository.UserProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class UserService {

    private static final String SOMENTE_ADMIN = "Acesso restrito a administradores";
    private static final Pattern FORMATO_EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserProfileRepository userProfileRepository;
    private final AuthorizationService authorizationService;
    private final AdministradoresIniciais administradoresIniciais;

    /** Sem administrador inicial: todo cadastro nasce CLIENTE. */
    public UserService(
            UserProfileRepository userProfileRepository,
            AuthorizationService authorizationService
    ) {
        this(userProfileRepository, authorizationService, AdministradoresIniciais.nenhum());
    }

    @Autowired
    public UserService(
            UserProfileRepository userProfileRepository,
            AuthorizationService authorizationService,
            AdministradoresIniciais administradoresIniciais
    ) {
        this.userProfileRepository = userProfileRepository;
        this.authorizationService = authorizationService;
        this.administradoresIniciais = administradoresIniciais;
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

    // Não há exclusão de usuário: desativar e reativar preservam o perfil e os pedidos (US07)
    public UserProfile reativar(UserProfile solicitante, Long usuarioId) {
        if (!authorizationService.podeGerenciarUsuarios(solicitante)) {
            throw new AcessoNegadoException(SOMENTE_ADMIN);
        }
        UserProfile usuario = buscarPorId(usuarioId);
        usuario.reativar();
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
        String emailNormalizado = normalizarEValidarEmail(email);

        // Perfil já criado para esta conta do Supabase, ou e-mail usado por outro perfil
        if (userProfileRepository.existsBySupabaseUserId(supabaseUserId)
                || userProfileRepository.existsByEmail(emailNormalizado)) {
            throw new EmailJaCadastradoException();
        }

        // Nunca vem do pedido: só os e-mails de ADMIN_EMAILS nascem ADMIN (RN-USER-03, #55)
        UserRole role = administradoresIniciais.contem(emailNormalizado) ? UserRole.ADMIN : UserRole.CLIENTE;

        UserProfile userProfile = new UserProfile(
                supabaseUserId,
                nome,
                emailNormalizado,
                role
        );
        return userProfileRepository.save(userProfile);
    }

    /** O próprio usuário altera o nome. A role nunca muda por aqui (RN-AUTHZ-04). */
    public UserProfile atualizarNome(UserProfile usuario, String nome) {
        validarNome(nome);
        usuario.alterarNome(nome.trim());
        return userProfileRepository.save(usuario);
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

    private String normalizarEValidarEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new UserValidationException(
                    "E-mail é obrigatório"
            );
        }

        String emailNormalizado = email.trim().toLowerCase(Locale.ROOT);
        if (!FORMATO_EMAIL.matcher(emailNormalizado).matches()) {
            throw new UserValidationException(
                    "E-mail inválido"
            );
        }
        return emailNormalizado;
    }
}