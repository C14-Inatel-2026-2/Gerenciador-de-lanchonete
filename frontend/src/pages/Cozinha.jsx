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
  const [erro, setErro] = useState("");
  const [pedidoEmAtualizacao, setPedidoEmAtualizacao] = useState(null);
  // Muda de valor para pedir uma nova busca da fila
  const [versaoDaFila, setVersaoDaFila] = useState(0);

  useEffect(() => {
    // Se a tela fechar ou uma busca mais nova começar, ignora a resposta antiga
    let ativo = true;

    listarFila()
      .then((fila) => {
        if (ativo) setPedidos(fila ?? []);
      })
      .catch((e) => {
        if (ativo) setErro(e.message);
      })
      .finally(() => {
        if (ativo) setCarregando(false);
      });

    return () => {
      ativo = false;
    };
  }, [versaoDaFila]);

  async function mudarStatus(id, novoStatus) {
    setErro("");
    setPedidoEmAtualizacao(id);
    try {
      await atualizarStatus(id, novoStatus);
    } catch (e) {
      setErro(e.message);
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

      {erro && <p role="alert">{erro}</p>}

      {pedidos.length === 0 ? (
        <p>Nenhum pedido na fila.</p>
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