import { api } from "./api";

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
