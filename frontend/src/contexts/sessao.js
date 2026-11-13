// Sessão do usuário logado.
//
// São duas partes:
//   - o perfil (id, nome, email, role), guardado aqui no localStorage para a tela;
//   - o token de acesso, guardado e renovado pelo próprio cliente do Supabase.
//
// Este módulo não depende do React de propósito: o api.js (#36) não é um
// componente e precisa ler o cabeçalho de autenticação e encerrar a sessão
// quando a API responde 401. O AuthProvider escuta o evento disparado aqui
// para manter a tela sincronizada.

import { supabase } from "../services/supabase";
import { ROLES } from "../utils/perfis";

export const CHAVE_SESSAO = "lanchonete.usuario";
export const EVENTO_SESSAO_ALTERADA = "lanchonete:sessao-alterada";

function usuarioValido(usuario) {
  return (
    usuario != null &&
    typeof usuario === "object" &&
    usuario.id != null &&
    typeof usuario.nome === "string" &&
    usuario.nome.trim() !== "" &&
    ROLES.includes(usuario.role)
  );
}

function avisarAlteracao() {
  window.dispatchEvent(new Event(EVENTO_SESSAO_ALTERADA));
}

/** Retorna o usuário salvo ou null. Dados corrompidos são descartados. */
export function lerSessao() {
  try {
    const salvo = localStorage.getItem(CHAVE_SESSAO);
    if (!salvo) return null;
    const usuario = JSON.parse(salvo);
    if (usuarioValido(usuario)) return usuario;
  } catch {
    // JSON inválido ou localStorage indisponível: trata como deslogado
  }
  try {
    localStorage.removeItem(CHAVE_SESSAO);
  } catch {
    // ignora
  }
  return null;
}

/** Salva o usuário logado (id, nome, email, role) e avisa o AuthProvider. */
export function salvarSessao(usuario) {
  if (!usuarioValido(usuario)) {
    throw new Error("Usuário inválido: id, nome e role são obrigatórios");
  }
  const dados = {
    id: usuario.id,
    nome: usuario.nome,
    email: usuario.email ?? null,
    role: usuario.role,
  };
  localStorage.setItem(CHAVE_SESSAO, JSON.stringify(dados));
  avisarAlteracao();
  return dados;
}

/**
 * Encerra a sessão: apaga o perfil e o token do Supabase deste navegador.
 * Usado pelo botão Sair e pelo api.js quando a API responde 401.
 */
export function limparSessao() {
  try {
    localStorage.removeItem(CHAVE_SESSAO);
  } catch {
    // ignora
  }
  avisarAlteracao();
  // scope "local": sai só deste navegador. Falha de rede não impede a saída.
  supabase?.auth.signOut({ scope: "local" }).catch(() => {});
}

/**
 * Cabeçalhos de autenticação para as chamadas à API. É a ÚNICA função que
 * sabe como o usuário é identificado: { Authorization: "Bearer <token>" } (#55).
 * O getSession() do Supabase já renova o token se ele tiver vencido.
 */
export async function cabecalhosDeAutenticacao() {
  if (!supabase) return {};
  const { data } = await supabase.auth.getSession();
  const token = data?.session?.access_token;
  return token ? { Authorization: `Bearer ${token}` } : {};
}
