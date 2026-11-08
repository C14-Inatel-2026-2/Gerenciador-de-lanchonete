import { describe, it, expect, beforeEach } from "vitest";
import { act, render, screen } from "@testing-library/react";
import { AuthProvider } from "../contexts/AuthContext";
import { useAuth } from "../contexts/useAuth";
import { CHAVE_SESSAO, limparSessao } from "../contexts/sessao";

const admin = { id: 3, nome: "Admin Teste", email: "admin.teste@lanchonete.com", role: "ADMIN" };

function ComponenteDeTeste() {
  const { usuario, estaLogado, entrar, sair, temPerfil } = useAuth();
  return (
    <div>
      <p data-testid="estado">{estaLogado ? `logado: ${usuario.nome}` : "deslogado"}</p>
      <p data-testid="admin">{temPerfil("ADMIN") ? "é admin" : "não é admin"}</p>
      <button onClick={() => entrar(admin)}>Entrar</button>
      <button onClick={sair}>Sair</button>
    </div>
  );
}

function renderizar() {
  return render(
    <AuthProvider>
      <ComponenteDeTeste />
    </AuthProvider>
  );
}

describe("AuthContext", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("começa deslogado quando não há sessão", () => {
    renderizar();

    expect(screen.getByTestId("estado")).toHaveTextContent("deslogado");
    expect(screen.getByTestId("admin")).toHaveTextContent("não é admin");
  });

  it("entrar guarda o usuário e atualiza a tela", () => {
    renderizar();

    act(() => screen.getByText("Entrar").click());

    expect(screen.getByTestId("estado")).toHaveTextContent("logado: Admin Teste");
    expect(screen.getByTestId("admin")).toHaveTextContent("é admin");
    expect(JSON.parse(localStorage.getItem(CHAVE_SESSAO))).toEqual(admin);
  });

  it("recupera o usuário do localStorage ao recarregar a página", () => {
    localStorage.setItem(CHAVE_SESSAO, JSON.stringify(admin));

    renderizar();

    expect(screen.getByTestId("estado")).toHaveTextContent("logado: Admin Teste");
  });

  it("sair limpa a sessão", () => {
    localStorage.setItem(CHAVE_SESSAO, JSON.stringify(admin));
    renderizar();

    act(() => screen.getByText("Sair").click());

    expect(screen.getByTestId("estado")).toHaveTextContent("deslogado");
    expect(localStorage.getItem(CHAVE_SESSAO)).toBeNull();
  });

  it("desloga quando a sessão é encerrada fora do React (ex.: 401 no api.js)", () => {
    localStorage.setItem(CHAVE_SESSAO, JSON.stringify(admin));
    renderizar();

    act(() => limparSessao());

    expect(screen.getByTestId("estado")).toHaveTextContent("deslogado");
  });

  it("temPerfil aceita mais de uma role", () => {
    localStorage.setItem(CHAVE_SESSAO, JSON.stringify({ ...admin, role: "FUNCIONARIO" }));
    function Perfis() {
      const { temPerfil } = useAuth();
      return (
        <>
          <p data-testid="cozinha">{String(temPerfil("FUNCIONARIO", "ADMIN"))}</p>
          <p data-testid="cliente">{String(temPerfil("CLIENTE"))}</p>
        </>
      );
    }
    render(
      <AuthProvider>
        <Perfis />
      </AuthProvider>
    );

    expect(screen.getByTestId("cozinha")).toHaveTextContent("true");
    expect(screen.getByTestId("cliente")).toHaveTextContent("false");
  });

  it("useAuth fora do AuthProvider lança erro", () => {
    function SemProvider() {
      useAuth();
      return null;
    }
    // Evita poluir o terminal com o erro esperado do React
    const erroOriginal = console.error;
    console.error = () => {};
    expect(() => render(<SemProvider />)).toThrow("useAuth precisa ser usado dentro de <AuthProvider>");
    console.error = erroOriginal;
  });
});
