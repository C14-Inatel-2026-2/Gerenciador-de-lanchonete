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

  const texto = await resposta.text();

  let dados = null;

  if (texto) {
    try {
      dados = JSON.parse(texto);
    } catch {
      dados = null;
    }
  }

  if (resposta.status === 401) {
    limparSessao();
    window.location.href = "/login";
  }

  if (!resposta.ok) {
    throw new Error(dados?.erro ?? `Erro ${resposta.status}`);
  }

  return dados;
}