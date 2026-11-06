import { useState } from "react";
import AdminDashboard from "./pages/AdminDashboard";
import CartPage from "./pages/CartPage";
import OrdersPage from "./pages/OrdersPage";
import { initialCart, mockOrders, mockProducts } from "./data/mockStore";
import "./App.css";

const pages = [
    { id: "cart", label: "Carrinho" },
    { id: "orders", label: "Pedidos" },
    { id: "admin", label: "Painel administrativo" },
];

function App() {
    const profileName = "Usuário";
    const profileInitial = profileName.trim().charAt(0).toLocaleUpperCase("pt-BR") || "U";
    const [page, setPage] = useState("cart");
    const [cart, setCart] = useState(initialCart);
    const [orders, setOrders] = useState(mockOrders);
    const [notice, setNotice] = useState("");

    const updateQuantity = (productId, quantity) => {
        setCart((currentCart) => currentCart
            .map((item) => item.id === productId ? { ...item, quantidade: quantity } : item)
            .filter((item) => item.quantidade > 0));
    };

    const addToCart = (product) => {
        setCart((currentCart) => {
            const existingItem = currentCart.find((item) => item.id === product.id);
            if (existingItem) {
                return currentCart.map((item) => item.id === product.id
                    ? { ...item, quantidade: item.quantidade + 1 }
                    : item);
            }
            return [...currentCart, { ...product, quantidade: 1 }];
        });
        setNotice(`${product.nome} adicionado ao carrinho.`);
    };

    const finishOrder = () => {
        if (cart.length === 0) return;

        const order = {
            id: Math.max(...orders.map((item) => item.id), 0) + 1,
            criadoEm: new Date().toISOString(),
            status: "CRIADO",
            itens: cart.map((item) => ({
                produtoId: item.id,
                nome: item.nome,
                precoUnitario: item.preco,
                quantidade: item.quantidade,
            })),
            endereco: "Rua das Flores, 123 — Centro",
            demonstrativo: true,
        };
        setOrders((currentOrders) => [order, ...currentOrders]);
        setCart([]);
        setNotice(`Pedido #${order.id} criado no modo demonstrativo.`);
        setPage("orders");
    };

    const cartCount = cart.reduce((total, item) => total + item.quantidade, 0);

    return (
        <div className="app-shell">
            <header className="topbar">
                <a className="brand" href="#inicio" onClick={(event) => {
                    event.preventDefault();
                    setPage("cart");
                }}>
                    <span className="brand-mark" aria-hidden="true">B</span>
                    <span>
                        <strong>Boa Mordida</strong>
                        <small>lanchonete</small>
                    </span>
                </a>
                <nav className="main-nav" aria-label="Navegação principal">
                    {pages.map((item) => (
                        <button
                            className={`nav-link${page === item.id ? " is-active" : ""}`}
                            key={item.id}
                            onClick={() => {
                                setNotice("");
                                setPage(item.id);
                            }}
                            type="button"
                        >
                            {item.label}
                            {item.id === "cart" && cartCount > 0 && (
                                <span className="nav-count">{cartCount}</span>
                            )}
                        </button>
                    ))}
                </nav>
                <div className="profile-chip">
                    <span className="profile-avatar" aria-hidden="true">{profileInitial}</span>
                    <span>Olá, {profileName}</span>
                </div>
            </header>

            {notice && (
                <div className="notice" role="status">
                    <span>{notice}</span>
                    <button aria-label="Fechar aviso" onClick={() => setNotice("")} type="button">×</button>
                </div>
            )}

            {page === "cart" && (
                <CartPage
                    cart={cart}
                    onAdd={addToCart}
                    onFinishOrder={finishOrder}
                    onUpdateQuantity={updateQuantity}
                    products={mockProducts}
                />
            )}
            {page === "orders" && <OrdersPage orders={orders} />}
            {page === "admin" && (
                <main className="admin-page">
                    <div className="page-heading">
                        <p className="eyebrow">VISÃO GERAL</p>
                        <h1>Painel administrativo</h1>
                        <p className="page-description">Resumo demonstrativo da operação da lanchonete.</p>
                    </div>
                    <AdminDashboard />
                </main>
            )}

            <footer className="site-footer">
                <span>Boa Mordida</span>
                <span>Protótipo de interface · dados demonstrativos</span>
            </footer>
        </div>
    );
}

export default App;
