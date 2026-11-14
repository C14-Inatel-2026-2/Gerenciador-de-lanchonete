import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import Carrinho from "../pages/Carrinho";
import { CarrinhoProvider } from "../contexts/CarrinhoContext";
import { criarPedido } from "../services/pedidos";

vi.mock("../services/pedidos", () => ({
    criarPedido: vi.fn(),
}));

const CHAVE_CARRINHO = "lanchonete.carrinho";
const itens = [
    {
        id: 1,
        nome: "X-Burger",
        descricao: "Hambúrguer com queijo.",
        preco: 24.9,
        quantidade: 2,
        quantidadeEstoque: 3,
    },
];

function renderCarrinho(itensCarrinho = itens) {
    const total = itensCarrinho.reduce((soma, item) => soma + item.preco * item.quantidade, 0);
    localStorage.setItem(CHAVE_CARRINHO, JSON.stringify({ itens: itensCarrinho, total }));
    return render(
        <CarrinhoProvider>
            <Carrinho />
        </CarrinhoProvider>
    );
}

describe("Carrinho", () => {
    beforeEach(() => {
        localStorage.clear();
        vi.clearAllMocks();
    });

    it("exibe os itens, quantidades e total do carrinho", () => {
        renderCarrinho();

        expect(screen.getByRole("heading", { name: "Meu carrinho" })).toBeInTheDocument();
        expect(screen.getByText("X-Burger")).toBeInTheDocument();
        expect(screen.getByText("2 itens selecionados")).toBeInTheDocument();
        expect(screen.getAllByText(/R\$\s?49,80/)).toHaveLength(3);
    });

    it("atualiza quantidade e total e permite remover o item", async () => {
        renderCarrinho();

        fireEvent.click(screen.getByRole("button", { name: "Aumentar quantidade de X-Burger" }));
        expect(await screen.findByText("3 itens selecionados")).toBeInTheDocument();
        expect(screen.getAllByText(/R\$\s?74,70/)).toHaveLength(3);
        expect(screen.getByRole("button", { name: "Aumentar quantidade de X-Burger" })).toBeDisabled();

        fireEvent.click(screen.getByRole("button", { name: "Remover" }));
        expect(await screen.findByText("Seu carrinho está vazio")).toBeInTheDocument();
    });

    it("mostra o estado vazio e desabilita a finalização", () => {
        renderCarrinho([]);

        expect(screen.getByText("Seu carrinho está vazio")).toBeInTheDocument();
        expect(screen.getByRole("button", { name: /Finalizar pedido/ })).toBeDisabled();
    });

    it("desabilita aumentar quantidade quando o estoque foi atingido", () => {
        renderCarrinho([{ ...itens[0], quantidade: 3, quantidadeEstoque: 3 }]);

        expect(screen.getByRole("button", { name: "Aumentar quantidade de X-Burger" })).toBeDisabled();
    });

    it("envia o pedido, mostra o número retornado e limpa o carrinho após sucesso", async () => {
        criarPedido.mockResolvedValue({ id: 73 });
        renderCarrinho();

        fireEvent.click(screen.getByRole("button", { name: /Finalizar pedido/ }));

        expect(await screen.findByRole("status")).toHaveTextContent("Pedido #73 criado com sucesso.");
        expect(criarPedido).toHaveBeenCalledWith(itens);
        await waitFor(() => {
            expect(JSON.parse(localStorage.getItem(CHAVE_CARRINHO))).toEqual({ itens: [], total: 0 });
        });
    });

    it("preserva o carrinho e mostra o erro se a API rejeitar o pedido", async () => {
        criarPedido.mockRejectedValue(new Error("Estoque insuficiente para o produto X-Burger."));
        renderCarrinho();

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
        renderCarrinho();

        fireEvent.click(screen.getByRole("button", { name: /Finalizar pedido/ }));
        fireEvent.click(screen.getByRole("button", { name: "Enviando pedido..." }));

        expect(criarPedido).toHaveBeenCalledOnce();
        concluirPedido({ id: 74 });
        expect(await screen.findByRole("status")).toHaveTextContent("Pedido #74 criado com sucesso.");
    });
});
