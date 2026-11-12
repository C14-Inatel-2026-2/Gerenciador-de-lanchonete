import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import App from "../App";
import { AuthProvider } from "../contexts/AuthContext";
import { salvarSessao } from "../contexts/sessao";
import { criarPedido } from "../services/pedidos";

vi.mock("../services/pedidos", () => ({
  criarPedido: vi.fn(),
}));

const CHAVE_CARRINHO = "lanchonete.carrinho";
const itens = [
  {
    id: 10,
    nome: "X-Burger",
    descricao: "Hambúrguer artesanal.",
    preco: 24.9,
    quantidadeEstoque: 4,
    quantidade: 2,
  },
];

function renderAppNoCarrinho() {
  window.history.replaceState({}, "", "/carrinho");
  return render(
    <AuthProvider>
      <App />
    </AuthProvider>
  );
}

describe("checkout", () => {
  beforeEach(() => {
    localStorage.clear();
    localStorage.setItem(CHAVE_CARRINHO, JSON.stringify({ itens, total: 49.8 }));
    salvarSessao({ id: 42, nome: "Julia", role: "CLIENTE" });
    vi.clearAllMocks();
  });

  it("envia o pedido, mostra o número retornado e limpa o carrinho após sucesso", async () => {
    criarPedido.mockResolvedValue({ id: 73 });
    renderAppNoCarrinho();

    fireEvent.click(screen.getByRole("button", { name: /Finalizar pedido/ }));

    expect(await screen.findByRole("status")).toHaveTextContent("Pedido #73 criado com sucesso.");
    expect(criarPedido).toHaveBeenCalledWith(itens);
    await waitFor(() => {
      expect(JSON.parse(localStorage.getItem(CHAVE_CARRINHO))).toEqual({ itens: [], total: 0 });
    });
  });

  it("preserva o carrinho e mostra o erro se a API rejeitar o pedido", async () => {
    criarPedido.mockRejectedValue(new Error("Estoque insuficiente para o produto X-Burger."));
    renderAppNoCarrinho();

    fireEvent.click(screen.getByRole("button", { name: /Finalizar pedido/ }));

    expect(await screen.findByRole("alert")).toHaveTextContent(
      "Estoque insuficiente para o produto X-Burger."
    );
    expect(JSON.parse(localStorage.getItem(CHAVE_CARRINHO))).toEqual({ itens, total: 49.8 });
  });

  it("não envia outro checkout enquanto a requisição está em andamento", async () => {
    let concluirPedido;
    criarPedido.mockReturnValue(new Promise((resolve) => {
      concluirPedido = resolve;
    }));
    renderAppNoCarrinho();

    fireEvent.click(screen.getByRole("button", { name: /Finalizar pedido/ }));
    const botao = screen.getByRole("button", { name: "Enviando pedido..." });
    fireEvent.click(botao);

    expect(criarPedido).toHaveBeenCalledOnce();
    concluirPedido({ id: 74 });
    expect(await screen.findByRole("status")).toHaveTextContent("Pedido #74 criado com sucesso.");
  });
});
