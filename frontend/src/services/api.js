import {
  cabecalhosDeAutenticacao,
  limparSessao,
} from "../contexts/sessao";

export async function api(caminho, opcoes = {}) {
  const resposta = await fetch(
    `${import.meta.env.VITE_API_URL}${caminho}`,
    {
      ...opcoes,
      headers: {
        "Content-Type": "application/json",
        ...cabecalhosDeAutenticacao(),
        ...(opcoes.headers || {}),
      },
    }
  );

  const dados = await resposta.json();

  if (resposta.status === 401) {
    limparSessao();
    window.location.href = "/login";
  }

  if (!resposta.ok) {
    throw new Error(dados.erro);
  }

  return dados;
}