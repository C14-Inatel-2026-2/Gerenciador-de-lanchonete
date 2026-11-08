import { describe, it, expect, beforeEach, vi } from "vitest";
import {
  CHAVE_SESSAO,
  EVENTO_SESSAO_ALTERADA,
  cabecalhosDeAutenticacao,
  lerSessao,
  limparSessao,
  salvarSessao,
} from "../contexts/sessao";

const cliente = { id: 1, nome: "Cliente Teste", email: "cliente.teste@lanchonete.com", role: "CLIENTE" };

describe("sessao", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("retorna null quando não há sessão salva", () => {
    expect(lerSessao()).toBeNull();
  });

  it("salva e lê o usuário do localStorage", () => {
    salvarSessao(cliente);

    expect(JSON.parse(localStorage.getItem(CHAVE_SESSAO))).toEqual(cliente);
    expect(lerSessao()).toEqual(cliente);
  });

  it("guarda só os campos da sessão", () => {
    salvarSessao({ ...cliente, senha: "nao-deve-ser-salva" });

    expect(lerSessao()).not.toHaveProperty("senha");
  });

  it("descarta sessão corrompida", () => {
    localStorage.setItem(CHAVE_SESSAO, "{ invalido");

    expect(lerSessao()).toBeNull();
    expect(localStorage.getItem(CHAVE_SESSAO)).toBeNull();
  });

  it("descarta sessão com role desconhecida", () => {
    localStorage.setItem(CHAVE_SESSAO, JSON.stringify({ ...cliente, role: "HACKER" }));

    expect(lerSessao()).toBeNull();
  });

  it("não salva usuário sem id, nome ou role válidos", () => {
    expect(() => salvarSessao({ nome: "Sem id", role: "CLIENTE" })).toThrow();
    expect(() => salvarSessao({ id: 1, nome: " ", role: "CLIENTE" })).toThrow();
    expect(() => salvarSessao({ id: 1, nome: "Sem role" })).toThrow();
    expect(localStorage.getItem(CHAVE_SESSAO)).toBeNull();
  });

  it("limparSessao remove o usuário e avisa a aplicação", () => {
    const ouvinte = vi.fn();
    window.addEventListener(EVENTO_SESSAO_ALTERADA, ouvinte);
    salvarSessao(cliente);

    limparSessao();

    expect(lerSessao()).toBeNull();
    expect(ouvinte).toHaveBeenCalledTimes(2); // salvar + limpar
    window.removeEventListener(EVENTO_SESSAO_ALTERADA, ouvinte);
  });

  it("cabecalhosDeAutenticacao envia X-Usuario-Id do usuário logado", () => {
    salvarSessao(cliente);

    expect(cabecalhosDeAutenticacao()).toEqual({ "X-Usuario-Id": "1" });
  });

  it("cabecalhosDeAutenticacao não envia nada sem usuário logado", () => {
    expect(cabecalhosDeAutenticacao()).toEqual({});
  });
});
