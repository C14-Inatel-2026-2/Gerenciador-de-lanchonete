import { describe, it, expect, vi, beforeEach } from "vitest";
import { act, render, screen, waitFor } from "@testing-library/react";
import { AuthProvider } from "../contexts/AuthContext";
import { useAuth } from "../contexts/useAuth";
import { CHAVE_SESSAO } from "../contexts/sessao";
import { supabase } from "../services/supabase";

// Sincronização do perfil salvo com a sessão do Supabase (#42)
vi.mock("../services/supabase", () => ({
  supabase: {
    auth: {
      getSession: vi.fn(),
      signOut: vi.fn(),
      onAuthStateChange: vi.fn(),
    },
  },
}));

const cliente = { id: 1, nome: "Cliente", email: "c@email.com", role: "CLIENTE" };

function Estado() {
  const { estaLogado } = useAuth();
  return <p data-testid="estado">{estaLogado ? "logado" : "deslogado"}</p>;
}

describe("AuthProvider com Supabase", () => {
  let avisarMudanca;

  beforeEach(() => {
    localStorage.clear();
    vi.resetAllMocks();
    supabase.auth.signOut.mockResolvedValue({ error: null });
    supabase.auth.onAuthStateChange.mockImplementation((callback) => {
      avisarMudanca = callback;
      return { data: { subscription: { unsubscribe: vi.fn() } } };
    });
  });

  it("perfil salvo sem token do Supabase (login antigo) é deslogado", async () => {
    localStorage.setItem(CHAVE_SESSAO, JSON.stringify(cliente));
    supabase.auth.getSession.mockResolvedValue({ data: { session: null } });

    render(<AuthProvider><Estado /></AuthProvider>);

    await waitFor(() => expect(screen.getByTestId("estado")).toHaveTextContent("deslogado"));
  });

  it("perfil salvo com token válido continua logado", async () => {
    localStorage.setItem(CHAVE_SESSAO, JSON.stringify(cliente));
    supabase.auth.getSession.mockResolvedValue({ data: { session: { access_token: "t" } } });

    render(<AuthProvider><Estado /></AuthProvider>);

    await waitFor(() => expect(supabase.auth.getSession).toHaveBeenCalled());
    expect(screen.getByTestId("estado")).toHaveTextContent("logado");
  });

  it("quando o Supabase encerra a sessão (token não renovado), desloga", async () => {
    localStorage.setItem(CHAVE_SESSAO, JSON.stringify(cliente));
    supabase.auth.getSession.mockResolvedValue({ data: { session: { access_token: "t" } } });
    render(<AuthProvider><Estado /></AuthProvider>);
    await waitFor(() => expect(supabase.auth.getSession).toHaveBeenCalled());

    act(() => avisarMudanca("SIGNED_OUT", null));

    expect(screen.getByTestId("estado")).toHaveTextContent("deslogado");
  });
});
