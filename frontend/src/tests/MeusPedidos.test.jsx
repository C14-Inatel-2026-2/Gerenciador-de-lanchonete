import { fireEvent, render, screen } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import MeusPedidos from "../pages/MeusPedidos";
import { listarMeusPedidos } from "../services/pedidos";

vi.mock("../services/pedidos", () => ({
    listarMeusPedidos: vi.fn(),
}));

const pedidos = [
    {
        id: 12,
        clienteId: 1,
        status: "EM_PREPARO",
        total: 49.8,
        criadoEm: "2026-11-11T12:00:00",
        itens: [
            {
                produtoId: 1,
                nome: "X-Burger",
                precoUnitario: 24.9,
                quantidade: 2,
                subtotal: 49.8,
            },
        ],
    },
];

describe("MeusPedidos", () => {
    beforeEach(() => {
        vi.clearAllMocks();
    });

    it("lista os pedidos retornados pela API e mostra detalhes ao expandir", async () => {
        listarMeusPedidos.mockResolvedValue(pedidos);
        render(<MeusPedidos />);

        expect(screen.getByRole("heading", { name: "Meus pedidos" })).toBeInTheDocument();
        expect(await screen.findByText("Pedido #12")).toBeInTheDocument();
        expect(screen.getByText("Em preparo")).toBeInTheDocument();

        fireEvent.click(screen.getByRole("button", { name: /Pedido #12/ }));

        expect(screen.getByText("2× X-Burger")).toBeInTheDocument();
        expect(screen.getAllByText(/R\$\s?49,80/)).toHaveLength(3);
    });

    it("mostra uma mensagem quando a API não retorna pedidos", async () => {
        listarMeusPedidos.mockResolvedValue([]);
        render(<MeusPedidos />);

        expect(await screen.findByText("Você ainda não tem pedidos")).toBeInTheDocument();
        expect(screen.getByText("0 pedidos")).toBeInTheDocument();
    });

    it("mostra o erro da API e permite tentar novamente", async () => {
        listarMeusPedidos
            .mockRejectedValueOnce(new Error("Não foi possível carregar os pedidos."))
            .mockResolvedValueOnce(pedidos);
        render(<MeusPedidos />);

        expect(await screen.findByRole("alert")).toHaveTextContent(
            "Não foi possível carregar os pedidos."
        );
        fireEvent.click(screen.getByRole("button", { name: "Tentar novamente" }));

        expect(await screen.findByText("Pedido #12")).toBeInTheDocument();
        expect(listarMeusPedidos).toHaveBeenCalledTimes(2);
    });

    it("indica enquanto os pedidos estão sendo carregados", () => {
        listarMeusPedidos.mockReturnValue(new Promise(() => {}));
        render(<MeusPedidos />);

        expect(screen.getByRole("status")).toHaveTextContent("Carregando seus pedidos...");
    });
});
