import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen, fireEvent, within, waitFor } from "@testing-library/react";

import AdminUsuarios from "../pages/AdminUsuarios";
import { AuthContext } from "../contexts/useAuth";
import { alterarPerfil, desativarUsuario, listarUsuarios } from "../services/usuarios";

// Troca o service por funções falsas: o teste não chama a API de verdade
vi.mock("../services/usuarios", () => ({
  listarUsuarios: vi.fn(),
  alterarPerfil: vi.fn(),
  desativarUsuario: vi.fn(),
}));

const admin = { id: 1, nome: "Admin", email: "admin@admin.com", role: "ADMIN", ativo: true };
const cliente = { id: 2, nome: "Bia", email: "bia@email.com", role: "CLIENTE", ativo: true };
const desativado = { id: 3, nome: "Caio", email: "caio@email.com", role: "CLIENTE", ativo: false };

function renderizar({ idDaSessao = admin.id } = {}) {
  return render(
    <AuthContext.Provider
      value={{
        usuario: { id: idDaSessao, nome: admin.nome, email: admin.email, role: "ADMIN" },
        estaLogado: true,
        entrar: () => {},
        sair: () => {},
        temPerfil: (...roles) => roles.includes("ADMIN"),
      }}
    >
      <AdminUsuarios />
    </AuthContext.Provider>
  );
}

function linhaDe(nome) {
  return screen.getByText(nome).closest("tr");
}

describe("AdminUsuarios", () => {
  beforeEach(() => {
    vi.resetAllMocks();
    listarUsuarios.mockResolvedValue([admin, cliente, desativado]);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it("lista nome, e-mail, perfil e situação de cada usuário", async () => {
    renderizar();

    expect(await screen.findByText("Bia")).toBeInTheDocument();
    const linha = linhaDe("Bia");
    expect(within(linha).getByText("bia@email.com")).toBeInTheDocument();
    expect(within(linha).getByRole("combobox", { name: "Perfil de Bia" })).toHaveValue("CLIENTE");
    expect(within(linha).getByText("Ativo")).toBeInTheDocument();
    expect(within(linhaDe("Caio")).getByText("Desativado")).toBeInTheDocument();
  });

  it("o admin não pode alterar a própria role nem se desativar", async () => {
    renderizar();

    await screen.findByText("Bia");
    const propria = linhaDe("Admin");
    expect(within(propria).getByText("(você)")).toBeInTheDocument();
    expect(within(propria).getByRole("combobox")).toBeDisabled();
    expect(within(propria).queryByRole("button", { name: "Desativar" })).not.toBeInTheDocument();
  });

  it("usuário desativado não tem ações", async () => {
    renderizar();

    await screen.findByText("Caio");
    const linha = linhaDe("Caio");
    expect(within(linha).getByRole("combobox")).toBeDisabled();
    expect(within(linha).queryByRole("button", { name: "Desativar" })).not.toBeInTheDocument();
  });

  it("promover a funcionário pede confirmação, envia a nova role e atualiza a linha", async () => {
    const confirmar = vi.spyOn(window, "confirm").mockReturnValue(true);
    alterarPerfil.mockResolvedValue({ ...cliente, role: "FUNCIONARIO" });
    renderizar();

    fireEvent.change(await screen.findByRole("combobox", { name: "Perfil de Bia" }), {
      target: { value: "FUNCIONARIO" },
    });

    expect(confirmar).toHaveBeenCalledWith("Alterar o perfil de Bia de Cliente para Funcionário?");
    expect(await screen.findByRole("status")).toHaveTextContent("Bia agora é Funcionário.");
    expect(alterarPerfil).toHaveBeenCalledWith(2, "FUNCIONARIO");
    expect(screen.getByRole("combobox", { name: "Perfil de Bia" })).toHaveValue("FUNCIONARIO");
  });

  it("desativar pede confirmação e, se cancelar, não chama a API", async () => {
    const confirmar = vi.spyOn(window, "confirm").mockReturnValue(false);
    renderizar();

    await screen.findByText("Bia");
    fireEvent.click(within(linhaDe("Bia")).getByRole("button", { name: "Desativar" }));

    expect(confirmar).toHaveBeenCalledWith(expect.stringContaining("Desativar Bia?"));
    expect(desativarUsuario).not.toHaveBeenCalled();
  });

  it("desativar confirmado marca o usuário como desativado e tira as ações", async () => {
    vi.spyOn(window, "confirm").mockReturnValue(true);
    desativarUsuario.mockResolvedValue({ ...cliente, ativo: false });
    renderizar();

    await screen.findByText("Bia");
    fireEvent.click(within(linhaDe("Bia")).getByRole("button", { name: "Desativar" }));

    expect(await screen.findByRole("status")).toHaveTextContent("A conta de Bia foi desativada.");
    expect(desativarUsuario).toHaveBeenCalledWith(2);
    const linha = linhaDe("Bia");
    expect(within(linha).getByText("Desativado")).toBeInTheDocument();
    expect(within(linha).queryByRole("button", { name: "Desativar" })).not.toBeInTheDocument();
  });

  it("mostra a mensagem do backend quando a ação é recusada e mantém a linha", async () => {
    vi.spyOn(window, "confirm").mockReturnValue(true);
    alterarPerfil.mockRejectedValue(new Error("Apenas administradores podem gerenciar usuários"));
    renderizar();

    fireEvent.change(await screen.findByRole("combobox", { name: "Perfil de Bia" }), {
      target: { value: "ADMIN" },
    });

    expect(await screen.findByRole("alert")).toHaveTextContent("Apenas administradores");
    await waitFor(() =>
      expect(screen.getByRole("combobox", { name: "Perfil de Bia" })).toHaveValue("CLIENTE")
    );
  });

  it("cancelar a troca de perfil não chama a API e mantém o perfil", async () => {
    vi.spyOn(window, "confirm").mockReturnValue(false);
    renderizar();

    const select = await screen.findByRole("combobox", { name: "Perfil de Bia" });
    fireEvent.change(select, { target: { value: "ADMIN" } });

    expect(alterarPerfil).not.toHaveBeenCalled();
    expect(select).toHaveValue("CLIENTE");
  });

  it("enquanto uma ação é enviada, a tabela inteira fica travada", async () => {
    vi.spyOn(window, "confirm").mockReturnValue(true);
    let responder;
    alterarPerfil.mockReturnValue(new Promise((resolve) => { responder = resolve; }));
    listarUsuarios.mockResolvedValue([admin, cliente, { ...cliente, id: 4, nome: "Dani", email: "dani@email.com" }]);
    renderizar();

    fireEvent.change(await screen.findByRole("combobox", { name: "Perfil de Bia" }), {
      target: { value: "FUNCIONARIO" },
    });

    // A linha de outro usuário também fica travada até a resposta chegar
    expect(screen.getByRole("combobox", { name: "Perfil de Dani" })).toBeDisabled();
    expect(within(linhaDe("Dani")).getByRole("button", { name: "Desativar" })).toBeDisabled();

    responder({ ...cliente, role: "FUNCIONARIO" });

    await waitFor(() => expect(screen.getByRole("combobox", { name: "Perfil de Dani" })).toBeEnabled());
  });

  it("reconhece o próprio admin mesmo com o id da sessão em texto", async () => {
    renderizar({ idDaSessao: "1" });

    await screen.findByText("Bia");
    const propria = linhaDe("Admin");
    expect(within(propria).getByText("(você)")).toBeInTheDocument();
    expect(within(propria).getByRole("combobox")).toBeDisabled();
  });

  it("resposta vazia da API vira mensagem de erro, sem quebrar a tela", async () => {
    vi.spyOn(window, "confirm").mockReturnValue(true);
    desativarUsuario.mockResolvedValue(null);
    renderizar();

    await screen.findByText("Bia");
    fireEvent.click(within(linhaDe("Bia")).getByRole("button", { name: "Desativar" }));

    expect(await screen.findByRole("alert")).toHaveTextContent("não devolveu o usuário atualizado");
    expect(within(linhaDe("Bia")).getByText("Ativo")).toBeInTheDocument();
  });

  it("se a lista não carrega (ex.: quem abriu não é admin), mostra só o erro", async () => {
    listarUsuarios.mockRejectedValue(new Error("Acesso negado"));
    renderizar();

    expect(await screen.findByRole("alert")).toHaveTextContent("Não foi possível carregar os usuários: Acesso negado");
    expect(screen.queryByText("Nenhum usuário cadastrado.")).not.toBeInTheDocument();
    expect(screen.queryByRole("table")).not.toBeInTheDocument();
  });
});
