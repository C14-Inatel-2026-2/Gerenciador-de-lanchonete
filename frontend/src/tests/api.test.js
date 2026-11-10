import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { api } from "../services/api";
import { salvarSessao, limparSessao } from "../contexts/sessao";

describe("api", () => {
  beforeEach(() => {
    localStorage.clear();
    salvarSessao({
      id: 1,
      nome: "Yasmin",
      role: "ADMIN",
    });

    vi.restoreAllMocks();
  });

  afterEach(() => {
    limparSessao();
  });

  it("deve enviar o header X-Usuario-Id", async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: true,
      status: 200,
      text: async () => JSON.stringify({ sucesso: true }),
    });

    await api("/teste");

    expect(fetch).toHaveBeenCalled();

    const chamada = fetch.mock.calls[0];

    expect(chamada[1].headers["X-Usuario-Id"]).toBe("1");
  });

  it("deve lançar erro quando a API responder erro", async () => {
    global.fetch = vi.fn().mockResolvedValue({
      ok: false,
      status: 400,
      text: async () =>
        JSON.stringify({
          erro: "Erro de teste",
        }),
    });

    await expect(api("/teste")).rejects.toThrow("Erro de teste");
  });
});