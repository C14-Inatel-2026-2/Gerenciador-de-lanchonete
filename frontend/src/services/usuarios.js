import { api } from "./api";

// Administração de usuários (US07 · #43). Todas as rotas são só para ADMIN;
// os demais recebem 403 (docs/api.md).

// GET /api/usuarios: ativos e desativados, em ordem de nome
export function listarUsuarios() {
  return api("/api/usuarios");
}

// PATCH /api/usuarios/{id}/role: o admin não pode alterar a própria role (403)
export function alterarPerfil(id, role) {
  return api(`/api/usuarios/${id}/role`, {
    method: "PATCH",
    body: JSON.stringify({ role }),
  });
}

// PATCH /api/usuarios/{id}/desativar: o usuário continua no banco e perde o acesso
export function desativarUsuario(id) {
  return api(`/api/usuarios/${id}/desativar`, { method: "PATCH" });
}
