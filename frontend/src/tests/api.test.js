import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { api } from "../services/api";
import { salvarSessao, lerSessao } from "../contexts/sessao";
import { supabase } from "../services/supabase";

vi.mock("../services/supabase", () => ({
  supabase: {
    auth: {
      getSession: vi.fn(),
      signOut: vi.fn(),
    },
  },
}));

function respostaFalsa(status, corpo) {
  return {
    ok: status >= 200 && status < 300,
    status,
    text: async () => (corpo === undefined ? "" : JSON.stringify(corpo)),
  };
}

describe("api", () => {
  const localizacaoOriginal = window.location;

  beforeEach(() => {
    localStorage.clear();
    vi.clearAllMocks();
    supabase.auth.getSession.mockResolvedValue({
      data: { session: { access_token: "token-do-supabase" } },
    });
    supabase.auth.signOut.mockResolvedValue({ error: null });
    // Permite conferir o redirecionamento sem navegar de verdade
    Object.defineProperty(window, "location", { value: { href: "/" }, writable: true });
  });

  afterEach(() => {
    Object.defineProperty(window, "location", { value: localizacaoOriginal, writable: true });
  });

  it("envia o token do Supabase no header Authorization", async () => {
    global.fetch = vi.fn().mockResolvedValue(respostaFalsa(200, { sucesso: true }));

    await api("/teste");

    const headers = fetch.mock.calls[0][1].headers;
    expect(headers.Authorization).toBe("Bearer token-do-supabase");
    expect(headers).not.toHaveProperty("X-Usuario-Id");
  });

  it("deve lançar erro quando a API responder erro", async () => {
    global.fetch = vi.fn().mockResolvedValue(respostaFalsa(400, { erro: "Erro de teste" }));

    await expect(api("/teste")).rejects.toThrow("Erro de teste");
  });

  it("401 de quem estava logado encerra a sessão e volta para o login", async () => {
    salvarSessao({ id: 1, nome: "Yasmin", role: "ADMIN" });
    global.fetch = vi.fn().mockResolvedValue(respostaFalsa(401, { erro: "Usuário não autenticado" }));

    await expect(api("/teste")).rejects.toThrow("Usuário não autenticado");
    expect(lerSessao()).toBeNull();
    expect(window.location.href).toBe("/login");
  });

  it("401 durante o login só devolve a mensagem, sem recarregar a página", async () => {
    global.fetch = vi.fn().mockResolvedValue(
      respostaFalsa(401, { erro: "Perfil não encontrado. Conclua o cadastro." })
    );

    await expect(api("/api/usuarios/me")).rejects.toThrow("Perfil não encontrado");
    expect(window.location.href).toBe("/");
  });
});
