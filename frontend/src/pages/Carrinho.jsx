import { useRef, useState } from "react";
import { useCarrinho } from "../contexts/useCarrinho";
import { criarPedido } from "../services/pedidos";
import { formatCurrency } from "../utils/formatCurrency";
import "./Carrinho.css";

function Carrinho() {
    const { cart, total, atualizarQuantidade, limparCarrinho } = useCarrinho();
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [notice, setNotice] = useState(null);
    const checkoutInProgress = useRef(false);

    const finalizarPedido = async () => {
        if (cart.length === 0 || checkoutInProgress.current) return;

        checkoutInProgress.current = true;
        setIsSubmitting(true);
        setNotice(null);

        try {
            const pedido = await criarPedido(cart);
            if (pedido?.id == null) {
                throw new Error("A API não retornou o número do pedido.");
            }

            limparCarrinho();
            setNotice({ message: `Pedido #${pedido.id} criado com sucesso.`, type: "success" });
        } catch (error) {
            setNotice({
                message: error instanceof Error ? error.message : "Não foi possível finalizar o pedido.",
                type: "error",
            });
        } finally {
            checkoutInProgress.current = false;
            setIsSubmitting(false);
        }
    };

    return (
        <div className="page-container">
            <section className="page-heading">
                <h1>Meu carrinho</h1>
                <p className="page-description">Confira seus itens e deixe tudo do seu jeito.</p>
            </section>

            {notice && (
                <p className="checkout-feedback" role={notice.type === "error" ? "alert" : "status"}>
                    {notice.message}
                </p>
            )}

            <div className="cart-layout">
                <section className="panel cart-panel" aria-label="Itens no carrinho">
                    <div className="panel-heading">
                        <div>
                            <h2>Itens do pedido</h2>
                            <p>{cart.reduce((sum, item) => sum + item.quantidade, 0)} itens selecionados</p>
                        </div>
                        <span className="soft-tag">Carrinho</span>
                    </div>

                    {cart.length === 0 ? (
                        <div className="empty-state">
                            <span className="empty-icon" aria-hidden="true">＋</span>
                            <h3>Seu carrinho está vazio</h3>
                            <p>Adicione algum produto da nossa seleção para começar.</p>
                        </div>
                    ) : (
                        <div className="cart-items">
                            {cart.map((item) => (
                                <article className="cart-item" key={item.id}>
                                    <div className="product-art" aria-hidden="true">🍽️</div>
                                    <div className="cart-item-info">
                                        <div className="item-title-row">
                                            <h3>{item.nome}</h3>
                                            <strong>{formatCurrency(item.preco * item.quantidade)}</strong>
                                        </div>
                                        <p>{item.descricao}</p>
                                        <span className="unit-price">{formatCurrency(item.preco)} <span>/ unidade</span></span>
                                        <div className="item-actions">
                                            <div className="quantity-control" aria-label={`Quantidade de ${item.nome}`}>
                                                <button
                                                    aria-label={`Diminuir quantidade de ${item.nome}`}
                                                    disabled={item.quantidade <= 1}
                                                    onClick={() => atualizarQuantidade(item.id, item.quantidade - 1)}
                                                    type="button"
                                                >−</button>
                                                <span aria-live="polite">{item.quantidade}</span>
                                                <button
                                                    aria-label={`Aumentar quantidade de ${item.nome}`}
                                                    disabled={item.quantidade >= item.quantidadeEstoque}
                                                    onClick={() => atualizarQuantidade(item.id, item.quantidade + 1)}
                                                    type="button"
                                                >+</button>
                                            </div>
                                            <button className="text-button remove-button" onClick={() => atualizarQuantidade(item.id, 0)} type="button">
                                                Remover
                                            </button>
                                        </div>
                                    </div>
                                </article>
                            ))}
                        </div>
                    )}
                </section>

                <aside className="panel summary-panel">
                    <h2>Resumo do pedido</h2>
                    <div className="summary-line">
                        <span>Subtotal</span>
                        <span>{formatCurrency(total)}</span>
                    </div>
                    <div className="summary-total">
                        <strong>Total</strong>
                        <strong>{formatCurrency(total)}</strong>
                    </div>
                    <button className="primary-button checkout-button" disabled={cart.length === 0 || isSubmitting} onClick={finalizarPedido} type="button">
                        {isSubmitting ? "Enviando pedido..." : "Finalizar pedido"}
                        {!isSubmitting && <span aria-hidden="true"> →</span>}
                    </button>
                </aside>
            </div>

        </div>
    );
}

export default Carrinho;
