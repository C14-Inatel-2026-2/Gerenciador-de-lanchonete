import { api } from "./api";

export function listarMeusPedidos() {
  return api("/api/pedidos/meus");
}

export function criarPedido(itens) {
  return api("/api/pedidos", {
    method: "POST",
    body: JSON.stringify({
      itens: itens.map((item) => ({
        produtoId: item.id,
        quantidade: item.quantidade,
      })),
    }),
  });
}
