import { fireEvent, render, screen } from "@testing-library/react";
import { describe, expect, it } from "vitest";
import MeusPedidos from "../pages/MeusPedidos";

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
    it("lista pedidos com status e mostra os detalhes ao expandir", () => {
        render(<MeusPedidos pedidosIniciais={pedidos} />);

        expect(screen.getByRole("heading", { name: "Meus pedidos" })).toBeInTheDocument();
        expect(screen.getByText("Pedido #12")).toBeInTheDocument();
        expect(screen.getByText("Em preparo")).toBeInTheDocument();

        fireEvent.click(screen.getByRole("button", { name: /Pedido #12/ }));

        expect(screen.getByText("2× X-Burger")).toBeInTheDocument();
        expect(screen.getAllByText(/R\$\s?49,80/)).toHaveLength(3);
    });

    it("mostra uma mensagem quando não há pedidos", () => {
        render(<MeusPedidos pedidosIniciais={[]} />);

        expect(screen.getByText("Você ainda não tem pedidos")).toBeInTheDocument();
        expect(screen.getByText("0 pedidos")).toBeInTheDocument();
    });
});
