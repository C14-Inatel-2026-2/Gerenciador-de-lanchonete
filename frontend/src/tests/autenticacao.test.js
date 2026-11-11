import { describe, it, expect, vi, beforeEach } from "vitest";
import { cadastrarConta, entrarComEmail } from "../services/autenticacao";
import { supabase } from "../services/supabase";
import { api } from "../services/api";

vi.mock("../services/supabase", () => {
  const supabase = {
    auth: {
      signUp: vi.fn(),
      signInWithPassword: vi.fn(),
      signOut: vi.fn(),
    },
  };
  return { supabase, exigirSupabase: () => supabase };
});

vi.mock("../services/api", () => ({ api: vi.fn() }));

const perfil = { id: 7, nome: "Karolina", email: "karol@email.com", role: "CLIENTE", ativo: true };
const sessao = { access_token: "token" };

function erroSupabase(code) {
  return { data: { user: null, session: null }, error: { code } };
}

describe("autenticacao", () => {
  beforeEach(() => {
    vi.resetAllMocks();
    supabase.auth.signOut.mockResolvedValue({ error: null });
  });

  describe("entrarComEmail", () => {
    it("entra no Supabase e busca o perfil em /api/usuarios/me", async () => {
      supabase.auth.signInWithPassword.mockResolvedValue({ data: { session: sessao }, error: null });
      api.mockResolvedValue(perfil);

      expect(await entrarComEmail({ email: "karol@email.com", senha: "123456" })).toEqual(perfil);
      expect(supabase.auth.signInWithPassword).toHaveBeenCalledWith({
        email: "karol@email.com",
        password: "123456",
      });
      expect(api).toHaveBeenCalledWith("/api/usuarios/me");
    });

    it("credenciais inválidas mostram mensagem em português e não chamam a API", async () => {
      supabase.auth.signInWithPassword.mockResolvedValue(erroSupabase("invalid_credentials"));

      await expect(entrarComEmail({ email: "karol@email.com", senha: "errada" }))
        .rejects.toThrow("E-mail ou senha incorretos.");
      expect(api).not.toHaveBeenCalled();
    });

    it("usuário desativado: mostra o erro da API e apaga o token", async () => {
      supabase.auth.signInWithPassword.mockResolvedValue({ data: { session: sessao }, error: null });
      api.mockRejectedValue(new Error("Usuário desativado"));

      await expect(entrarComEmail({ email: "karol@email.com", senha: "123456" }))
        .rejects.toThrow("Usuário desativado");
      expect(supabase.auth.signOut).toHaveBeenCalledWith({ scope: "local" });
    });

    it("erro desconhecido do Supabase vira mensagem genérica", async () => {
      supabase.auth.signInWithPassword.mockResolvedValue(erroSupabase("unexpected_failure"));

      await expect(entrarComEmail({ email: "a@a.com", senha: "123456" }))
        .rejects.toThrow("Não foi possível falar com o serviço de login");
    });
  });

  describe("cadastrarConta", () => {
    it("cria a conta no Supabase e o perfil na API só com o nome", async () => {
      supabase.auth.signUp.mockResolvedValue({ data: { session: sessao }, error: null });
      api.mockResolvedValue(perfil);

      expect(await cadastrarConta({ nome: "Karolina", email: "karol@email.com", senha: "123456" }))
        .toEqual(perfil);
      expect(api).toHaveBeenCalledWith("/api/usuarios", {
        method: "POST",
        body: JSON.stringify({ nome: "Karolina" }),
      });
    });

    it("nunca envia role nem e-mail no corpo: o backend usa o token", async () => {
      supabase.auth.signUp.mockResolvedValue({ data: { session: sessao }, error: null });
      api.mockResolvedValue(perfil);

      await cadastrarConta({ nome: "Karolina", email: "karol@email.com", senha: "123456", role: "ADMIN" });

      const corpo = JSON.parse(api.mock.calls[0][1].body);
      expect(corpo).toEqual({ nome: "Karolina" });
    });

    it("senha fraca mostra a mensagem do Supabase em português", async () => {
      supabase.auth.signUp.mockResolvedValue(erroSupabase("weak_password"));

      await expect(cadastrarConta({ nome: "K", email: "k@email.com", senha: "123" }))
        .rejects.toThrow("A senha precisa ter pelo menos 6 caracteres.");
      expect(api).not.toHaveBeenCalled();
    });

    it("e-mail de outra pessoa (senha diferente) mostra 'E-mail já cadastrado'", async () => {
      supabase.auth.signUp.mockResolvedValue(erroSupabase("user_already_exists"));
      supabase.auth.signInWithPassword.mockResolvedValue(erroSupabase("invalid_credentials"));

      await expect(cadastrarConta({ nome: "K", email: "admin@admin.com", senha: "123456" }))
        .rejects.toThrow("E-mail já cadastrado");
      expect(api).not.toHaveBeenCalled();
    });

    it("conta que ficou sem perfil (cadastro interrompido) só cria o perfil", async () => {
      supabase.auth.signUp.mockResolvedValue(erroSupabase("user_already_exists"));
      supabase.auth.signInWithPassword.mockResolvedValue({ data: { session: sessao }, error: null });
      api.mockResolvedValue(perfil);

      expect(await cadastrarConta({ nome: "Karolina", email: "karol@email.com", senha: "123456" }))
        .toEqual(perfil);
    });

    it("perfil já existente (409 da API) mostra a mensagem e apaga o token", async () => {
      supabase.auth.signUp.mockResolvedValue(erroSupabase("user_already_exists"));
      supabase.auth.signInWithPassword.mockResolvedValue({ data: { session: sessao }, error: null });
      api.mockRejectedValue(new Error("E-mail já cadastrado"));

      await expect(cadastrarConta({ nome: "Karolina", email: "karol@email.com", senha: "123456" }))
        .rejects.toThrow("E-mail já cadastrado");
      expect(supabase.auth.signOut).toHaveBeenCalledWith({ scope: "local" });
    });

    it("com confirmação de e-mail ligada, avisa em vez de chamar a API sem token", async () => {
      supabase.auth.signUp.mockResolvedValue({ data: { user: { id: "x" }, session: null }, error: null });

      await expect(cadastrarConta({ nome: "K", email: "k@email.com", senha: "123456" }))
        .rejects.toThrow("Confirme e depois entre");
      expect(api).not.toHaveBeenCalled();
    });
  });
});
