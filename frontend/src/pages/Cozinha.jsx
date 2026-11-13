import { useEffect, useState } from "react";
import { atualizarStatus, listarFila } from "../services/cozinha";
import { formatCurrency } from "../utils/formatCurrency";

// Mesmas transições da máquina de estados do backend (Pedido.alterarStatus).
// O front só mostra os botões válidos; quem garante a regra é o backend (409).
const PROXIMOS_STATUS = {
  CRIADO: ["EM_PREPARO", "CANCELADO"],
  EM_PREPARO: ["PRONTO", "CANCELADO"],
  PRONTO: ["ENTREGUE"],
};

const ROTULO_STATUS = {
  CRIADO: "Recebido",
  EM_PREPARO: "Em preparo",
  PRONTO: "Pronto",
};

const ROTULO_ACAO = {
  EM_PREPARO: "Iniciar preparo",
  PRONTO: "Marcar como pronto",
  ENTREGUE: "Entregar",
  CANCELADO: "Cancelar",
};

function formatarHora(dataHora) {
  return new Date(dataHora).toLocaleTimeString("pt-BR", {
    hour: "2-digit",
    minute: "2-digit",
  });
}

export default function Cozinha() {
  const [pedidos, setPedidos] = useState([]);
  const [carregando, setCarregando] = useState(true);
  // Falha ao buscar a fila: some quando uma busca seguinte der certo
  const [erroFila, setErroFila] = useState("");
  // Falha ao mudar o status (ex.: 409): fica até a próxima ação do usuário
  const [erroAcao, setErroAcao] = useState("");
  const [pedidoEmAtualizacao, setPedidoEmAtualizacao] = useState(null);
  // Muda de valor para pedir uma nova busca da fila
  const [versaoDaFila, setVersaoDaFila] = useState(0);

  useEffect(() => {
    // Se a tela fechar ou uma busca mais nova começar, ignora a resposta antiga
    let ativo = true;

    listarFila()
      .then((fila) => {
        if (!ativo) return;
        setPedidos(fila ?? []);
        setErroFila("");
      })
      .catch((e) => {
        if (ativo) setErroFila(e.message);
      })
      .finally(() => {
        if (ativo) setCarregando(false);
      });

    return () => {
      ativo = false;
    };
  }, [versaoDaFila]);

  // Atualiza a fila sozinha a cada 10 segundos (#46)
  useEffect(() => {
    const intervalo = setInterval(() => {
      setVersaoDaFila((versao) => versao + 1);
    }, 10000);

    return () => clearInterval(intervalo);
  }, []);

  async function mudarStatus(id, novoStatus) {
    setErroAcao("");
    setPedidoEmAtualizacao(id);
    try {
      await atualizarStatus(id, novoStatus);
    } catch (e) {
      setErroAcao(e.message);
    } finally {
      setPedidoEmAtualizacao(null);
      // Recarrega mesmo com erro: num 409, a fila passa a mostrar o status atual
      setVersaoDaFila((versao) => versao + 1);
    }
  }

  if (carregando) {
    return <p>Carregando a fila...</p>;
  }

  return (
    <section>
      <h1>Fila da cozinha</h1>

      {erroAcao && <p role="alert">{erroAcao}</p>}
      {erroFila && <p role="alert">Não foi possível carregar a fila: {erroFila}</p>}

      {pedidos.length === 0 ? (
        !erroFila && <p>Nenhum pedido na fila.</p>
      ) : (
        <ul>
          {pedidos.map((pedido) => (
            <li key={pedido.id}>
              <h2>Pedido #{pedido.id}</h2>
              <p>
                {ROTULO_STATUS[pedido.status]} · {formatarHora(pedido.criadoEm)} ·{" "}
                {formatCurrency(pedido.total)}
              </p>

              <ul>
                {pedido.itens.map((item) => (
                  <li key={item.produtoId}>
                    {item.quantidade}x {item.nome}
                  </li>
                ))}
              </ul>

              {(PROXIMOS_STATUS[pedido.status] ?? []).map((status) => (
                <button
                  key={status}
                  type="button"
                  disabled={pedidoEmAtualizacao === pedido.id}
                  onClick={() => mudarStatus(pedido.id, status)}
                >
                  {ROTULO_ACAO[status]}
                </button>
              ))}
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}