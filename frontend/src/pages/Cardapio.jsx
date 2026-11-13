import { useEffect, useMemo, useState } from "react";
import { useCarrinho } from "../contexts/useCarrinho";
import { api } from "../services/api";
import { formatCurrency } from "../utils/formatCurrency";
import "./Cardapio.css";

export default function Cardapio() {
  const { adicionarItem } = useCarrinho();
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
            (produto) => String(produto.categoriaId) === categoriaSelecionada
          )
        : produtosDisponiveis,
    [categoriaSelecionada, produtosDisponiveis]
  );

  const produtosPorCategoria = useMemo(() => {
    const grupos = new Map();

    for (const produto of produtosFiltrados) {
      const categoriaId = produto.categoriaId;
      const categoria = categorias.find(
        (item) => String(item.id) === String(categoriaId)
      );
      const nomeCategoria = categoria?.nome ?? "Outros";

      if (!grupos.has(categoriaId)) {
        grupos.set(categoriaId, { nome: nomeCategoria, produtos: [] });
      }
      grupos.get(categoriaId).produtos.push(produto);
    }

    return [...grupos.values()];
  }, [categorias, produtosFiltrados]);

  return (
    <div className="page-container cardapio">
      <header className="page-heading">
        <p className="eyebrow">NOSSO MENU</p>
        <h1 id="titulo-cardapio">Cardápio</h1>
        <p className="page-description">
          Escolha seus favoritos e adicione ao carrinho.
        </p>
      </header>

      {carregando ? (
        <div className="panel cardapio-message">
          <p role="status">Carregando cardápio...</p>
        </div>
      ) : erro ? (
        <div className="panel cardapio-message">
          <p role="alert">Não foi possível carregar o cardápio: {erro}</p>
        </div>
      ) : produtosDisponiveis.length === 0 ? (
        <div className="panel empty-state cardapio-empty">
          <span className="cardapio-empty__icon" aria-hidden="true">🍽️</span>
          <h3>Cardápio vazio</h3>
          <p role="status">Cardápio vazio: não há produtos disponíveis.</p>
        </div>
      ) : (
        <>
          <div className="panel cardapio-filter">
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
            <p className="panel cardapio-message" role="status">
              Não há produtos disponíveis nesta categoria.
            </p>
          ) : (
            <div className="cardapio-groups">
              {produtosPorCategoria.map((grupo) => (
                <section
                  className="cardapio-group"
                  key={grupo.nome}
                  aria-labelledby={`categoria-${grupo.nome}`}
                >
                  <div className="cardapio-group__heading">
                    <h2 id={`categoria-${grupo.nome}`}>{grupo.nome}</h2>
                    <span>{grupo.produtos.length} produtos</span>
                  </div>
                  <div className="cardapio-products">
                    {grupo.produtos.map((produto) => (
                      <article className="cardapio-product panel" key={produto.id}>
                        <div className="cardapio-product__art" aria-hidden="true">
                          🍴
                        </div>
                        <div className="cardapio-product__details">
                          <h3>{produto.nome}</h3>
                          {produto.descricao && <p>{produto.descricao}</p>}
                          {produto.esgotado && (
                            <span className="cardapio-product__sold-out" role="status">
                              Esgotado
                            </span>
                          )}
                        </div>
                        <div className="cardapio-product__footer">
                          <strong>{formatCurrency(produto.preco)}</strong>
                          <button
                            className="cardapio-add-button"
                            type="button"
                            onClick={() => adicionarItem(produto)}
                            disabled={produto.esgotado}
                          >
                            Adicionar ao carrinho
                          </button>
                        </div>
                      </article>
                    ))}
                  </div>
                </section>
              ))}
            </div>
          )}
        </>
      )}
    </div>
  );
}
