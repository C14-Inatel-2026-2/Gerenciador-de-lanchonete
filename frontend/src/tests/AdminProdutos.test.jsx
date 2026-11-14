import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import AdminProdutos from "../pages/AdminProdutos";
import { api } from "../services/api";

vi.mock("../services/api", () => ({
  api: vi.fn(),
}));

const categorias = [{ id: 1, nome: "Lanches", ativo: true }];

const produtos = [
  {
    id: 10,
    nome: "X-Burger",
    descricao: "Pão e carne",
    preco: 25,
    disponivel: true,
    categoriaId: 1,
    quantidadeEstoque: 10,
    esgotado: false,
  },
];

function configurarRespostas() {
  api.mockImplementation((caminho) => {
    if (caminho === "/api/produtos") return Promise.resolve(produtos);
    if (caminho === "/api/categorias") return Promise.resolve(categorias);
    return Promise.resolve(null);
  });
}

describe("AdminProdutos", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    configurarRespostas();
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("lista produtos disponíveis com categoria e preço formatado", async () => {
    render(<AdminProdutos />);

    expect(await screen.findByText("X-Burger")).toBeInTheDocument();
    expect(screen.getByText("Categoria: Lanches")).toBeInTheDocument();
    expect(screen.getByText("R$ 25,00")).toBeInTheDocument();
    expect(screen.getByText("Estoque: 10")).toBeInTheDocument();
    expect(api).toHaveBeenCalledWith("/api/produtos");
    expect(api).toHaveBeenCalledWith("/api/categorias");
  });

  it("cadastra produto enviando os dados para a API", async () => {
    render(<AdminProdutos />);

    await screen.findByText("X-Burger");
    fireEvent.change(screen.getByLabelText("Nome"), {
      target: { value: "Suco" },
    });
    fireEvent.change(screen.getByLabelText("Descrição"), {
      target: { value: "Suco natural" },
    });
    fireEvent.change(screen.getByLabelText("Preço"), {
      target: { value: "8.5" },
    });
    fireEvent.change(screen.getByLabelText("Categoria"), {
      target: { value: "1" },
    });
    fireEvent.submit(screen.getByRole("button", { name: "Cadastrar produto" }).closest("form"));

    await waitFor(() => {
      expect(api).toHaveBeenCalledWith("/api/produtos", {
        method: "POST",
        body: JSON.stringify({
          nome: "Suco",
          descricao: "Suco natural",
          preco: 8.5,
          categoriaId: 1,
          quantidadeEstoque: 0,
        }),
      });
    });
    expect(await screen.findByText("Produto cadastrado com sucesso.")).toBeInTheDocument();
  });

  it("cadastra categoria enviando o nome para a API", async () => {
    render(<AdminProdutos />);

    await screen.findByText("X-Burger");
    fireEvent.change(screen.getByLabelText("Nome da categoria"), {
      target: { value: "Sobremesas" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Cadastrar categoria" }));

    await waitFor(() => {
      expect(api).toHaveBeenCalledWith("/api/categorias", {
        method: "POST",
        body: JSON.stringify({ nome: "Sobremesas" }),
      });
    });
    expect(await screen.findByText("Categoria cadastrada com sucesso.")).toBeInTheDocument();
  });

  it("edita categoria enviando o novo nome para a API", async () => {
    render(<AdminProdutos />);

    fireEvent.click(await screen.findByRole("button", { name: "Editar categoria" }));
    fireEvent.change(screen.getByLabelText("Nome da categoria"), {
      target: { value: "Lanches e sanduíches" },
    });
    fireEvent.click(screen.getByRole("button", { name: "Salvar categoria" }));

    await waitFor(() => {
      expect(api).toHaveBeenCalledWith("/api/categorias/1", {
        method: "PUT",
        body: JSON.stringify({ nome: "Lanches e sanduíches" }),
      });
    });
    expect(await screen.findByText("Categoria atualizada com sucesso.")).toBeInTheDocument();
  });

  it("desativa categoria e remove da lista de categorias ativas", async () => {
    render(<AdminProdutos />);

    expect(await screen.findByRole("heading", { name: "Lanches" })).toBeInTheDocument();
    fireEvent.click(screen.getByRole("button", { name: "Desativar categoria" }));

    await waitFor(() => {
      expect(api).toHaveBeenCalledWith("/api/categorias/1/desativar", {
        method: "PATCH",
      });
    });
    expect(screen.queryByRole("heading", { name: "Lanches" })).not.toBeInTheDocument();
    expect(screen.getByText('Categoria "Lanches" desativada.')).toBeInTheDocument();
  });

  it("edita produto selecionado usando PUT", async () => {
    render(<AdminProdutos />);

    fireEvent.click(await screen.findByRole("button", { name: "Editar" }));
    fireEvent.change(screen.getByLabelText("Descrição"), {
      target: { value: "Pão, carne e queijo" },
    });
    fireEvent.submit(screen.getByRole("button", { name: "Salvar alterações" }).closest("form"));

    await waitFor(() => {
      expect(api).toHaveBeenCalledWith("/api/produtos/10", {
        method: "PUT",
        body: JSON.stringify({
          nome: "X-Burger",
          descricao: "Pão, carne e queijo",
          preco: 25,
          categoriaId: 1,
          quantidadeEstoque: 10,
        }),
      });
    });
    expect(await screen.findByText("Produto atualizado com sucesso.")).toBeInTheDocument();
  });

  it("desativa produto e remove da lista disponível", async () => {
    api.mockImplementation((caminho) => {
      if (caminho === "/api/produtos") return Promise.resolve(produtos);
      if (caminho === "/api/categorias") return Promise.resolve(categorias);
      return Promise.resolve(null);
    });
    render(<AdminProdutos />);

    fireEvent.click(await screen.findByRole("button", { name: "Desativar" }));

    await waitFor(() => {
      expect(api).toHaveBeenCalledWith("/api/produtos/10/desativar", {
        method: "PATCH",
      });
    });
    expect(screen.queryByText("X-Burger")).not.toBeInTheDocument();
    expect(screen.getByText('Produto "X-Burger" desativado.')).toBeInTheDocument();
  });

  it("ajusta o estoque usando PATCH", async () => {
    render(<AdminProdutos />);

    const estoque = await screen.findByLabelText("Ajustar estoque");
    fireEvent.change(estoque, { target: { value: "14" } });
    fireEvent.click(screen.getByRole("button", { name: "Salvar estoque" }));

    await waitFor(() => {
      expect(api).toHaveBeenCalledWith("/api/produtos/10/estoque", {
        method: "PATCH",
        body: JSON.stringify({ quantidade: 14 }),
      });
    });
    expect(await screen.findByText('Estoque de "X-Burger" atualizado.')).toBeInTheDocument();
    expect(screen.getByText("Estoque: 14")).toBeInTheDocument();
  });

  it("mostra erro retornado pela API", async () => {
    api.mockRejectedValueOnce(new Error("Falha ao carregar produtos"));
    render(<AdminProdutos />);

    expect(
      await screen.findByText("Falha ao carregar produtos")
    ).toBeInTheDocument();
  });
});
