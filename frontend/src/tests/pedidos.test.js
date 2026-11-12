import { describe, expect, it, vi } from "vitest";
import { api } from "../services/api";
import { criarPedido } from "../services/pedidos";

vi.mock("../services/api", () => ({
  api: vi.fn(),
}));

describe("criarPedido", () => {
  it("envia somente o identificador e a quantidade dos itens", async () => {
    api.mockResolvedValue({ id: 17 });
    const itens = [
      {
        id: 10,
        nome: "X-Burger",
        preco: 24.9,
        quantidade: 2,
        quantidadeEstoque: 5,
      },
    ];

    await expect(criarPedido(itens)).resolves.toEqual({ id: 17 });

    expect(api).toHaveBeenCalledWith("/api/pedidos", {
      method: "POST",
      body: JSON.stringify({
        itens: [{ produtoId: 10, quantidade: 2 }],
      }),
    });
  });
});
