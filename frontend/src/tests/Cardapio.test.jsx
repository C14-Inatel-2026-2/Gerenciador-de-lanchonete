import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import Cardapio, { EVENTO_ADICIONAR_CARRINHO } from "../pages/Cardapio";

const categorias = [
  { id: 1, nome: "Lanches", ativo: true },
  { id: 2, nome: "Bebidas", ativo: true },
];

const produtos = [
  {
    id: 10,
    nome: "X-Burger",
    descricao: "Pão, carne e queijo",
    preco: 25,
    disponivel: true,
    categoriaId: 1,
    quantidadeEstoque: 3,
    esgotado: false,
  },
  {
    id: 11,
    nome: "Suco",
    descricao: "Suco natural",
    preco: 8,
    disponivel: true,
    categoriaId: 2,
    quantidadeEstoque: 2,
    esgotado: false,
  },
  {
    id: 12,
    nome: "Produto indisponível",
    descricao: "",
    preco: 5,
    disponivel: false,
    categoriaId: 1,
    quantidadeEstoque: 0,
    esgotado: true,
  },
  {
    id: 13,
    nome: "Produto esgotado",
    descricao: "",
    preco: 30,
    disponivel: true,
    categoriaId: 1,
    quantidadeEstoque: 0,
    esgotado: true,
  },
];

function respostaApi(dados) {
  return {
    ok: true,
    status: 200,
    text: async () => JSON.stringify(dados),
  };
}

function configurarFetch(dadosProdutos = produtos) {
  vi.stubGlobal(
    "fetch",
    vi.fn((url) => {
      if (url.endsWith("/api/categorias")) {
        return Promise.resolve(respostaApi(categorias));
      }
      if (url.endsWith("/api/produtos")) {
        return Promise.resolve(respostaApi(dadosProdutos));
      }
      throw new Error(`URL inesperada: ${url}`);
    })
  );
}

describe("Cardapio", () => {
  beforeEach(() => {
    configurarFetch();
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("agrupa produtos disponíveis por categoria e formata os preços", async () => {
    render(<Cardapio />);

    expect(await screen.findByRole("heading", { name: "Lanches" })).toBeInTheDocument();
    expect(screen.getByRole("heading", { name: "Bebidas" })).toBeInTheDocument();
    expect(screen.getByText("X-Burger")).toBeInTheDocument();
    expect(screen.getByText("Suco")).toBeInTheDocument();
    expect(screen.getByText("R$ 25,00")).toBeInTheDocument();
    expect(screen.getByText("R$ 8,00")).toBeInTheDocument();
    expect(screen.queryByText("Produto indisponível")).not.toBeInTheDocument();
    expect(screen.getByText("Esgotado")).toBeInTheDocument();
    expect(
      screen.getByRole("heading", { name: "X-Burger" })
        .closest("article")
        .querySelector("button")
    ).toBeEnabled();
    expect(
      screen.getByRole("heading", { name: "Produto esgotado" })
        .closest("article")
        .querySelector("button")
    ).toBeDisabled();
  });

  it("mostra mensagem quando não há produtos disponíveis", async () => {
    configurarFetch([]);
    render(<Cardapio />);

    expect(
      await screen.findByText("Cardápio vazio: não há produtos disponíveis.")
    ).toBeInTheDocument();
    expect(screen.queryByLabelText("Filtrar por categoria")).not.toBeInTheDocument();
  });

  it("filtra produtos pela categoria escolhida", async () => {
    render(<Cardapio />);

    const filtro = await screen.findByLabelText("Filtrar por categoria");
    fireEvent.change(filtro, { target: { value: "2" } });

    expect(screen.getByText("Suco")).toBeInTheDocument();
    expect(screen.queryByText("X-Burger")).not.toBeInTheDocument();
  });

  it("emite evento para adicionar o produto ao carrinho", async () => {
    const aoAdicionar = vi.fn();
    window.addEventListener(EVENTO_ADICIONAR_CARRINHO, aoAdicionar);
    render(<Cardapio />);

    fireEvent.click(
      (await screen.findAllByRole("button", { name: "Adicionar ao carrinho" }))[0]
    );

    await waitFor(() => expect(aoAdicionar).toHaveBeenCalledOnce());
    expect(aoAdicionar.mock.calls[0][0].detail).toEqual({
      produto: produtos[0],
      quantidade: 1,
    });
    window.removeEventListener(EVENTO_ADICIONAR_CARRINHO, aoAdicionar);
  });
});
