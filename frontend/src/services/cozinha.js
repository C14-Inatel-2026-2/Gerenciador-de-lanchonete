import { api } from "./api";

// GET /api/pedidos/fila — pedidos CRIADO, EM_PREPARO e PRONTO, mais antigos primeiro
export function listarFila() {
  return api("/api/pedidos/fila");
}

// PATCH /api/pedidos/{id}/status — transição inválida responde 409
export function atualizarStatus(id, status) {
  return api(`/api/pedidos/${id}/status`, {
    method: "PATCH",
    body: JSON.stringify({ status }),
  });
}