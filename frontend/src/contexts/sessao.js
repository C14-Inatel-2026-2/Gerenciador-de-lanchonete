// Sessão do usuário logado, guardada no localStorage.
//
// Este módulo não depende do React de propósito: o api.js (#36) não é um
// componente e precisa ler o cabeçalho de autenticação e encerrar a sessão
// quando a API responde 401. O AuthProvider escuta o evento disparado aqui
// para manter a tela sincronizada.

export const CHAVE_SESSAO = "lanchonete.usuario";
export const EVENTO_SESSAO_ALTERADA = "lanchonete:sessao-alterada";

const ROLES_VALIDAS = ["CLIENTE", "FUNCIONARIO", "ADMIN"];

function usuarioValido(usuario) {
  return (
    usuario != null &&
    typeof usuario === "object" &&
    usuario.id != null &&
    typeof usuario.nome === "string" &&
    usuario.nome.trim() !== "" &&
    ROLES_VALIDAS.includes(usuario.role)
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

/** Encerra a sessão. O api.js deve chamar isto quando a API responder 401. */
export function limparSessao() {
  try {
    localStorage.removeItem(CHAVE_SESSAO);
  } catch {
    // ignora
  }
  avisarAlteracao();
}

/**
 * Cabeçalhos de autenticação para as chamadas à API. É a ÚNICA função que
 * sabe como o usuário é identificado:
 *   - hoje (login provisório): { "X-Usuario-Id": "<id>" }
 *   - depois da #55: { Authorization: "Bearer <token do Supabase>" }
 * O api.js só espalha o retorno nos headers e não muda quando a #55 entrar.
 */
export function cabecalhosDeAutenticacao() {
  const usuario = lerSessao();
  return usuario ? { "X-Usuario-Id": String(usuario.id) } : {};
}
