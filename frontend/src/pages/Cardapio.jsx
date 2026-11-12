import { useEffect, useMemo, useState } from "react";
import { api } from "../services/api";
import { formatCurrency } from "../utils/formatCurrency";

export const EVENTO_ADICIONAR_CARRINHO = "carrinho:adicionar";

export default function Cardapio() {
  const [categorias, setCategorias] = useState([]);
  const [produtos, setProdutos] = useState([]);
  const [categoriaSelecionada, setCategoriaSelecionada] = useState("");
  const [carregando, setCarregando] = useState(true);
  const [erro, setErro] = useState("");

  useEffect(() => {
    let ativo = true;

    async function carregarCardapio() {
      try {
        const [categoriasRecebidas, produtosRecebidos] = await Promise.all([
          api("/api/categorias"),
          api("/api/produtos"),
        ]);

        if (ativo) {
          setCategorias(categoriasRecebidas ?? []);
          setProdutos(produtosRecebidos ?? []);
        }
      } catch (erroRequisicao) {
        if (ativo) {
          setErro(erroRequisicao.message);
        }
      } finally {
        if (ativo) {
          setCarregando(false);
        }
      }
    }

    carregarCardapio();
    return () => {
      ativo = false;
    };
  }, []);

  const produtosDisponiveis = useMemo(
    () => produtos.filter((produto) => produto.disponivel),
    [produtos]
  );

  const produtosFiltrados = useMemo(
    () =>
      categoriaSelecionada
        ? produtosDisponiveis.filter(
            (produto) => String(produto.categoria?.id) === categoriaSelecionada
          )
        : produtosDisponiveis,
    [categoriaSelecionada, produtosDisponiveis]
  );

  const produtosPorCategoria = useMemo(() => {
    const grupos = new Map();

    for (const produto of produtosFiltrados) {
      const categoriaId = produto.categoria?.id;
      const categoria = categorias.find(
        (item) => String(item.id) === String(categoriaId)
      );
      const nomeCategoria = categoria?.nome ?? produto.categoria?.nome ?? "Outros";

      if (!grupos.has(categoriaId)) {
        grupos.set(categoriaId, { nome: nomeCategoria, produtos: [] });
      }
      grupos.get(categoriaId).produtos.push(produto);
    }

    return [...grupos.values()];
  }, [categorias, produtosFiltrados]);

  function adicionarAoCarrinho(produto) {
    window.dispatchEvent(
      new CustomEvent(EVENTO_ADICIONAR_CARRINHO, {
        detail: { produto, quantidade: 1 },
      })
    );
  }

  if (carregando) {
    return (
      <section aria-labelledby="titulo-cardapio">
        <h1 id="titulo-cardapio">Cardápio</h1>
        <p role="status">Carregando cardápio...</p>
      </section>
    );
  }

  if (erro) {
    return (
      <section aria-labelledby="titulo-cardapio">
        <h1 id="titulo-cardapio">Cardápio</h1>
        <p role="alert">Não foi possível carregar o cardápio: {erro}</p>
      </section>
    );
  }

  return (
    <section aria-labelledby="titulo-cardapio">
      <h1 id="titulo-cardapio">Cardápio</h1>

      {produtosDisponiveis.length === 0 ? (
        <p role="status">
          Cardápio vazio: não há produtos disponíveis.
        </p>
      ) : (
        <>
          <div>
            <label htmlFor="filtro-categoria">Filtrar por categoria</label>
            <select
              id="filtro-categoria"
              value={categoriaSelecionada}
              onChange={(evento) => setCategoriaSelecionada(evento.target.value)}
            >
              <option value="">Todas as categorias</option>
              {categorias.map((categoria) => (
                <option key={categoria.id} value={String(categoria.id)}>
                  {categoria.nome}
                </option>
              ))}
            </select>
          </div>

          {produtosFiltrados.length === 0 ? (
            <p role="status">
              Não há produtos disponíveis nesta categoria.
            </p>
          ) : (
            <div>
              {produtosPorCategoria.map((grupo) => (
                <section
                  key={grupo.nome}
                  aria-labelledby={`categoria-${grupo.nome}`}
                >
                  <h2 id={`categoria-${grupo.nome}`}>{grupo.nome}</h2>
                  <div>
                    {grupo.produtos.map((produto) => (
                      <article key={produto.id}>
                        <h3>{produto.nome}</h3>
                        {produto.descricao && <p>{produto.descricao}</p>}
                        <p>{formatCurrency(produto.preco)}</p>
                        <button
                          type="button"
                          onClick={() => adicionarAoCarrinho(produto)}
                        >
                          Adicionar ao carrinho
                        </button>
                      </article>
                    ))}
                  </div>
                </section>
              ))}
            </div>
          )}
        </>
      )}
    </section>
  );
}
