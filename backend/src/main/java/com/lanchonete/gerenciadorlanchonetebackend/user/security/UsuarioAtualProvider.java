package com.lanchonete.gerenciadorlanchonetebackend.user.security;

import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;

/**
 * Único ponto que sabe identificar o usuário logado (RNF-AUT06).
 * Controllers dependem só desta interface: trocar o mecanismo de login
 * (header provisório para JWT do Supabase, #55) não altera os controllers.
 */
public interface UsuarioAtualProvider {

    /**
     * @return o perfil do usuário da requisição atual (sempre ativo)
     * @throws com.lanchonete.gerenciadorlanchonetebackend.exception.UsuarioNaoAutenticadoException
     *         se não for possível identificar o usuário (401)
     * @throws com.lanchonete.gerenciadorlanchonetebackend.exception.AcessoNegadoException
     *         se o usuário estiver desativado (403)
     */
    UserProfile obterUsuarioAtual();
}
