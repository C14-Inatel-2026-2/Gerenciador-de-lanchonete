import { useState } from "react";
import { mockOrders } from "../data/mockStore";
import { formatCurrency } from "../utils/formatCurrency";
import "./MeusPedidos.css";

const statusLabels = {
    CRIADO: "Recebido",
    EM_PREPARO: "Em preparo",
    PRONTO: "Pronto",
    ENTREGUE: "Entregue",
    CANCELADO: "Cancelado",
};

const statusSteps = ["CRIADO", "EM_PREPARO", "PRONTO", "ENTREGUE"];

function formatDate(value) {
    return new Intl.DateTimeFormat("pt-BR", {
        day: "2-digit",
        month: "short",
        year: "numeric",
        hour: "2-digit",
        minute: "2-digit",
    }).format(new Date(value));
}

function MeusPedidos({ pedidosIniciais = mockOrders }) {
    const [pedidos] = useState(pedidosIniciais);
    const [pedidoExpandido, setPedidoExpandido] = useState(null);

    return (
        <div className="page-container">
            <section className="page-heading">
                <p className="eyebrow">ACOMPANHE POR AQUI</p>
                <h1>Meus pedidos</h1>
                <p className="page-description">Veja o andamento e os detalhes dos seus pedidos recentes.</p>
            </section>

            <div className="orders-toolbar">
                <div>
                    <h2>Pedidos recentes</h2>
                    <p>{pedidos.length} pedidos</p>
                </div>
                <span className="soft-tag">Mais recentes primeiro</span>
            </div>

            {pedidos.length === 0 ? (
                <section className="panel empty-state orders-empty">
                    <span className="empty-icon" aria-hidden="true">⌁</span>
                    <h3>Você ainda não tem pedidos</h3>
                    <p>Seus pedidos aparecerão aqui depois que você finalizar uma compra.</p>
                </section>
            ) : (
                <section className="orders-list" aria-label="Lista de pedidos">
                    {pedidos.map((pedido) => {
                        const estaExpandido = pedidoExpandido === pedido.id;
                        const statusClass = pedido.status.toLowerCase().replace("_", "-");
                        const step = statusSteps.indexOf(pedido.status);

                        return (
                            <article className={`order-card${estaExpandido ? " is-expanded" : ""}`} key={pedido.id}>
                                <button
                                    aria-controls={`order-details-${pedido.id}`}
                                    aria-expanded={estaExpandido}
                                    className="order-card-toggle"
                                    onClick={() => setPedidoExpandido(estaExpandido ? null : pedido.id)}
                                    type="button"
                                >
                                    <span className="order-icon" aria-hidden="true">#</span>
                                    <span className="order-main">
                                        <span className="order-title-row">
                                            <strong>Pedido #{pedido.id}</strong>
                                            <span className={`status-badge status-${statusClass}`}>
                                                <span className="status-dot" />
                                                {statusLabels[pedido.status] ?? pedido.status}
                                            </span>
                                        </span>
                                        <span className="order-meta">{formatDate(pedido.criadoEm)} <span>·</span> {pedido.itens.length} produtos</span>
                                    </span>
                                    <span className="order-total">{formatCurrency(pedido.total)}</span>
                                    <span className={`chevron${estaExpandido ? " rotated" : ""}`} aria-hidden="true">⌄</span>
                                </button>

                                {estaExpandido && (
                                    <div className="order-details" id={`order-details-${pedido.id}`}>
                                        {pedido.status !== "CANCELADO" && (
                                            <div className="order-progress" aria-label={`Status: ${statusLabels[pedido.status]}`}>
                                                {statusSteps.map((status, index) => (
                                                    <div className={`progress-step${index <= step ? " is-complete" : ""}`} key={status}>
                                                        <span className="progress-marker">{index < step ? "✓" : index + 1}</span>
                                                        <span>{statusLabels[status]}</span>
                                                    </div>
                                                ))}
                                            </div>
                                        )}
                                        <div className="details-heading">
                                            <h3>Detalhes do pedido</h3>
                                        </div>
                                        <div className="detail-products">
                                            {pedido.itens.map((item) => (
                                                <div className="detail-product" key={item.produtoId ?? item.id ?? item.nome}>
                                                    <span>{item.quantidade}× {item.nome}</span>
                                                    <span>{formatCurrency(item.subtotal)}</span>
                                                </div>
                                            ))}
                                        </div>
                                        <div className="detail-footer">
                                            <div className="detail-total">
                                                <span className="detail-label">Total do pedido</span>
                                                <strong>{formatCurrency(pedido.total)}</strong>
                                            </div>
                                        </div>
                                    </div>
                                )}
                            </article>
                        );
                    })}
                </section>
            )}
            <p className="page-footnote">Os pedidos exibidos são dados demonstrativos enquanto a API de pedidos não está disponível.</p>
        </div>
    );
}

export default MeusPedidos;
