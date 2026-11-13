import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen, fireEvent, waitFor, act } from "@testing-library/react";

import Cozinha from "../pages/Cozinha";
import { listarFila, atualizarStatus } from "../services/cozinha";

// Troca o service por funções falsas: o teste não chama a API de verdade
vi.mock("../services/cozinha", () => ({
  listarFila: vi.fn(),
  atualizarStatus: vi.fn(),
}));

function pedido(id, status) {
  return {
    id,
    clienteId: 1,
    status,
    total: 10,
    criadoEm: "2026-11-10T19:30:00",
    itens: [{ produtoId: id, nome: `Item ${id}`, precoUnitario: 10, quantidade: 1, subtotal: 10 }],
  };
}

describe("Cozinha", () => {
  beforeEach(() => {
    vi.resetAllMocks();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("mostra só os botões das transições válidas de cada pedido", async () => {
    listarFila.mockResolvedValue([pedido(1, "CRIADO"), pedido(2, "PRONTO")]);

    render(<Cozinha />);

    expect(await screen.findByText("Pedido #1")).toBeInTheDocument();
    expect(screen.getByRole("button", { name: "Iniciar preparo" })).toBeInTheDocument();
    // Só o CRIADO pode ser cancelado; o PRONTO não
    expect(screen.getAllByRole("button", { name: "Cancelar" })).toHaveLength(1);
    expect(screen.getAllByRole("button", { name: "Entregar" })).toHaveLength(1);
    expect(screen.queryByRole("button", { name: "Marcar como pronto" })).not.toBeInTheDocument();
  });

  it("ao clicar, envia o novo status e recarrega a fila", async () => {
    listarFila
      .mockResolvedValueOnce([pedido(1, "CRIADO")])
      .mockResolvedValueOnce([pedido(1, "EM_PREPARO")]);
    atualizarStatus.mockResolvedValue(pedido(1, "EM_PREPARO"));

    render(<Cozinha />);
    fireEvent.click(await screen.findByRole("button", { name: "Iniciar preparo" }));

    await waitFor(() => expect(atualizarStatus).toHaveBeenCalledWith(1, "EM_PREPARO"));
    expect(await screen.findByRole("button", { name: "Marcar como pronto" })).toBeInTheDocument();
    expect(listarFila).toHaveBeenCalledTimes(2);
  });

  it("mostra a mensagem do backend quando a transição é recusada (409)", async () => {
    listarFila.mockResolvedValue([pedido(1, "CRIADO")]);
    atualizarStatus.mockRejectedValue(new Error("Transição de status inválida"));

    render(<Cozinha />);
    fireEvent.click(await screen.findByRole("button", { name: "Cancelar" }));

    expect(await screen.findByRole("alert")).toHaveTextContent("Transição de status inválida");
  });

  it("avisa quando não há pedidos na fila", async () => {
    listarFila.mockResolvedValue([]);

    render(<Cozinha />);

    expect(await screen.findByText("Nenhum pedido na fila.")).toBeInTheDocument();
  });

  it("se a fila não carrega, mostra só o erro, sem dizer que a fila está vazia", async () => {
    listarFila.mockRejectedValue(new Error("Falha de rede"));

    render(<Cozinha />);

    expect(await screen.findByRole("alert")).toHaveTextContent("Falha de rede");
    expect(screen.queryByText("Nenhum pedido na fila.")).not.toBeInTheDocument();
  });

  it("atualiza a fila sozinha a cada 10 segundos", async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true });
    listarFila.mockResolvedValue([]);

    render(<Cozinha />);
    await screen.findByText("Nenhum pedido na fila.");
    expect(listarFila).toHaveBeenCalledTimes(1);

    await act(async () => {
      vi.advanceTimersByTime(10000);
    });

    await waitFor(() => expect(listarFila).toHaveBeenCalledTimes(2));
  });
});