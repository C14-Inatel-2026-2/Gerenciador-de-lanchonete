import { useCallback, useEffect, useMemo, useState } from "react";
import { AuthContext } from "./useAuth";
import {
  CHAVE_SESSAO,
  EVENTO_SESSAO_ALTERADA,
  lerSessao,
  limparSessao,
  salvarSessao,
} from "./sessao";
import { supabase } from "../services/supabase";

/**
 * Guarda o usuário logado e o mantém salvo no localStorage.
 *
 * Valor exposto (via useAuth):
 *   usuario     { id, nome, email, role } ou null
 *   estaLogado  boolean
 *   entrar(u)   salva o usuário e o torna o usuário logado
 *   sair()      encerra a sessão
 *   temPerfil(...roles)  true se o usuário logado tem uma das roles
 *
 * As telas de Login e Cadastro (#42) chamam entrar() com o perfil devolvido pela
 * API depois da autenticação no Supabase (services/autenticacao.js).
 */
export function AuthProvider({ children }) {
  const [usuario, setUsuario] = useState(() => lerSessao());

  useEffect(() => {
    if (!supabase) return undefined;

    // Perfil salvo sem token do Supabase (ex.: sessão do login provisório
    // com X-Usuario-Id, ou token apagado): encerra para pedir login de novo.
    supabase.auth.getSession().then(({ data }) => {
      if (!data?.session && lerSessao() !== null) limparSessao();
    });

    // O Supabase avisa quando a sessão acaba (ex.: não conseguiu renovar o token)
    const { data } = supabase.auth.onAuthStateChange((evento) => {
      if (evento === "SIGNED_OUT" && lerSessao() !== null) limparSessao();
    });
    return () => data.subscription.unsubscribe();
  }, []);

  useEffect(() => {
    const sincronizar = () => setUsuario(lerSessao());
    const aoMudarStorage = (evento) => {
      // Outra aba entrou ou saiu
      if (evento.key === null || evento.key === CHAVE_SESSAO) sincronizar();
    };

    window.addEventListener(EVENTO_SESSAO_ALTERADA, sincronizar);
    window.addEventListener("storage", aoMudarStorage);
    return () => {
      window.removeEventListener(EVENTO_SESSAO_ALTERADA, sincronizar);
      window.removeEventListener("storage", aoMudarStorage);
    };
  }, []);

  const entrar = useCallback((dadosUsuario) => {
    const salvo = salvarSessao(dadosUsuario);
    setUsuario(salvo);
    return salvo;
  }, []);

  const sair = useCallback(() => {
    limparSessao();
    setUsuario(null);
  }, []);

  const temPerfil = useCallback(
    (...roles) => usuario !== null && roles.includes(usuario.role),
    [usuario]
  );

  const valor = useMemo(
    () => ({ usuario, estaLogado: usuario !== null, entrar, sair, temPerfil }),
    [usuario, entrar, sair, temPerfil]
  );

  return <AuthContext.Provider value={valor}>{children}</AuthContext.Provider>;
}
