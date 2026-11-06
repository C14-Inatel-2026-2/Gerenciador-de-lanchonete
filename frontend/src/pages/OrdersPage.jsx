import { useState } from "react";
import { formatCurrency } from "../utils/formatCurrency";
import "./OrdersPage.css";

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

function getOrderTotal(order) {
    return order.itens.reduce((sum, item) => sum + (item.precoUnitario ?? item.preco) * item.quantidade, 0);
}

function OrdersPage({ orders }) {
    const [expandedOrder, setExpandedOrder] = useState(null);

    return (
        <main className="page-container">
            <section className="page-heading">
                <p className="eyebrow">ACOMPANHE POR AQUI</p>
                <h1>Meus pedidos</h1>
                <p className="page-description">Veja o andamento e os detalhes dos seus pedidos recentes.</p>
            </section>

            <div className="orders-toolbar">
                <div>
                    <h2>Pedidos recentes</h2>
                    <p>{orders.length} pedidos</p>
                </div>
                <span className="soft-tag">Mais recentes primeiro</span>
            </div>

            {orders.length === 0 ? (
                <section className="panel empty-state orders-empty">
                    <span className="empty-icon" aria-hidden="true">⌁</span>
                    <h3>Você ainda não tem pedidos</h3>
                    <p>Seus pedidos aparecerão aqui depois que você finalizar uma compra.</p>
                </section>
            ) : (
                <section className="orders-list" aria-label="Lista de pedidos">
                    {orders.map((order) => {
                        const isExpanded = expandedOrder === order.id;
                        const statusClass = order.status.toLowerCase().replace("_", "-");
                        const step = statusSteps.indexOf(order.status);

                        return (
                            <article className={`order-card${isExpanded ? " is-expanded" : ""}`} key={order.id}>
                                <button
                                    aria-controls={`order-details-${order.id}`}
                                    aria-expanded={isExpanded}
                                    className="order-card-toggle"
                                    onClick={() => setExpandedOrder(isExpanded ? null : order.id)}
                                    type="button"
                                >
                                    <span className="order-icon" aria-hidden="true">#</span>
                                    <span className="order-main">
                                        <span className="order-title-row">
                                            <strong>Pedido #{order.id}</strong>
                                            <span className={`status-badge status-${statusClass}`}>
                                                <span className="status-dot" />
                                                {statusLabels[order.status] ?? order.status}
                                            </span>
                                        </span>
                                        <span className="order-meta">{formatDate(order.criadoEm)} <span>·</span> {order.itens.length} produtos</span>
                                    </span>
                                    <span className="order-total">{formatCurrency(getOrderTotal(order))}</span>
                                    <span className={`chevron${isExpanded ? " rotated" : ""}`} aria-hidden="true">⌄</span>
                                </button>

                                {isExpanded && (
                                    <div className="order-details" id={`order-details-${order.id}`}>
                                        {order.status !== "CANCELADO" && (
                                            <div className="order-progress" aria-label={`Status: ${statusLabels[order.status]}`}>
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
                                            {order.demonstrativo && <span className="demo-label">Demonstrativo</span>}
                                        </div>
                                        <div className="detail-products">
                                            {order.itens.map((item) => (
                                                <div className="detail-product" key={item.produtoId ?? item.id ?? item.nome}>
                                                    <span>{item.quantidade}× {item.nome}</span>
                                                    <span>{formatCurrency((item.precoUnitario ?? item.preco) * item.quantidade)}</span>
                                                </div>
                                            ))}
                                        </div>
                                        <div className="detail-footer">
                                            <div>
                                                <span className="detail-label">Endereço de entrega</span>
                                                <strong>{order.endereco || "Não informado"}</strong>
                                            </div>
                                            <div className="detail-total">
                                                <span className="detail-label">Total do pedido</span>
                                                <strong>{formatCurrency(getOrderTotal(order))}</strong>
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
        </main>
    );
}

export default OrdersPage;
