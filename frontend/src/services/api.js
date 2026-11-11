import {
  cabecalhosDeAutenticacao,
  lerSessao,
  limparSessao,
} from "../contexts/sessao";

export async function api(caminho, opcoes = {}) {
  const resposta = await fetch(
    `${import.meta.env.VITE_API_URL}${caminho}`,
    {
      ...opcoes,
      headers: {
        "Content-Type": "application/json",
        ...(await cabecalhosDeAutenticacao()),
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

  // Sessão vencida de quem estava logado: volta para o login.
  // Sem perfil salvo (ex.: no meio do login ou do cadastro), o 401 vira só
  // a mensagem de erro, para a tela poder mostrá-la.
  if (resposta.status === 401 && lerSessao() !== null) {
    limparSessao();
    window.location.href = "/login";
  }

  if (!resposta.ok) {
    throw new Error(dados?.erro ?? `Erro ${resposta.status}`);
  }

  return dados;
}
