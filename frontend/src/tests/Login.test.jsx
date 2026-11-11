import { describe, it, expect, vi, beforeEach } from "vitest";
import { render, screen, fireEvent } from "@testing-library/react";
import { MemoryRouter, Routes, Route } from "react-router-dom";

import Login from "../pages/Login";
import { AuthProvider } from "../contexts/AuthContext";
import { CHAVE_SESSAO } from "../contexts/sessao";
import { entrarComEmail } from "../services/autenticacao";

// Sem Supabase nem API de verdade: a tela só conversa com o serviço
vi.mock("../services/autenticacao", () => ({ entrarComEmail: vi.fn() }));

function renderizar() {
  return render(
    <AuthProvider>
      <MemoryRouter initialEntries={["/login"]}>
        <Routes>
          <Route path="/login" element={<Login />} />
          <Route path="/cardapio" element={<h1>Cardápio</h1>} />
          <Route path="/cozinha" element={<h1>Cozinha</h1>} />
        </Routes>
      </MemoryRouter>
    </AuthProvider>
  );
}

function preencher({ email = "", senha = "" }) {
  fireEvent.change(screen.getByLabelText("E-mail"), { target: { value: email } });
  fireEvent.change(screen.getByLabelText("Senha"), { target: { value: senha } });
  fireEvent.click(screen.getByRole("button", { name: "Entrar" }));
}

describe("Login", () => {
  beforeEach(() => {
    localStorage.clear();
    vi.resetAllMocks();
  });

  it("e-mail vazio bloqueia o envio", () => {
    renderizar();

    preencher({ senha: "123456" });

    expect(screen.getByRole("alert")).toHaveTextContent("Informe o e-mail.");
    expect(entrarComEmail).not.toHaveBeenCalled();
  });

  it("senha vazia bloqueia o envio", () => {
    renderizar();

    preencher({ email: "cliente@email.com" });

    expect(screen.getByRole("alert")).toHaveTextContent("Informe a senha.");
    expect(entrarComEmail).not.toHaveBeenCalled();
  });

  it("credenciais inválidas mostram a mensagem e continuam na tela", async () => {
    entrarComEmail.mockRejectedValue(new Error("E-mail ou senha incorretos."));
    renderizar();

    preencher({ email: "cliente@email.com", senha: "errada" });

    expect(await screen.findByRole("alert")).toHaveTextContent("E-mail ou senha incorretos.");
    expect(screen.getByRole("button", { name: "Entrar" })).toBeEnabled();
    expect(localStorage.getItem(CHAVE_SESSAO)).toBeNull();
  });

  it("login bem-sucedido guarda o usuário e abre a página do perfil", async () => {
    const funcionario = { id: 2, nome: "Funcionário", email: "f@email.com", role: "FUNCIONARIO" };
    entrarComEmail.mockResolvedValue(funcionario);
    renderizar();

    preencher({ email: " f@email.com ", senha: "123456" });

    expect(await screen.findByRole("heading", { name: "Cozinha" })).toBeInTheDocument();
    expect(entrarComEmail).toHaveBeenCalledWith({ email: "f@email.com", senha: "123456" });
    expect(JSON.parse(localStorage.getItem(CHAVE_SESSAO))).toEqual(funcionario);
  });

  it("quem já está logado vai direto para a sua página", () => {
    localStorage.setItem(CHAVE_SESSAO, JSON.stringify({ id: 1, nome: "Cliente", role: "CLIENTE" }));

    renderizar();

    expect(screen.getByRole("heading", { name: "Cardápio" })).toBeInTheDocument();
  });
});
