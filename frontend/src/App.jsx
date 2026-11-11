import { useState } from "react";
import { BrowserRouter, Route, Routes, useNavigate } from "react-router-dom";
import MainLayout from "./layouts/MainLayout";
import ProtectedRoute from "./components/ProtectedRoute";

import Login from "./pages/Login";
import Cadastro from "./pages/Cadastro";
import Cardapio from "./pages/Cardapio";
import Cozinha from "./pages/Cozinha";
import Admin from "./pages/Admin";
import AdminProdutos from "./pages/AdminProdutos";
import AdminUsuarios from "./pages/AdminUsuarios";
import AdminDashboard from "./pages/AdminDashboard";
import CartPage from "./pages/CartPage";
import OrdersPage from "./pages/OrdersPage";
import { initialCart, mockOrders } from "./data/mockStore";
import "./App.css";

function AppRoutes() {
    const navigate = useNavigate();
    const [cart, setCart] = useState(initialCart);
    const [orders, setOrders] = useState(mockOrders);
    const [notice, setNotice] = useState("");

    const updateQuantity = (productId, quantity) => {
        setCart((currentCart) => currentCart
            .map((item) => item.id === productId ? { ...item, quantidade: quantity } : item)
            .filter((item) => item.quantidade > 0));
    };

    const finishOrder = () => {
        if (cart.length === 0) return;

        const itens = cart.map((item) => ({
            produtoId: item.id,
            nome: item.nome,
            precoUnitario: item.preco,
            quantidade: item.quantidade,
            subtotal: item.preco * item.quantidade,
        }));
        const order = {
            id: Math.max(...orders.map((item) => item.id), 0) + 1,
            clienteId: 1,
            criadoEm: new Date().toISOString(),
            status: "CRIADO",
            total: itens.reduce((total, item) => total + item.subtotal, 0),
            itens,
        };
        setOrders((currentOrders) => [order, ...currentOrders]);
        setCart([]);
        setNotice(`Pedido #${order.id} criado no modo demonstrativo.`);
        navigate("/meus-pedidos");
    };

    return (
        <div className="app-shell">
            {notice && (
                <div className="notice" role="status">
                    <span>{notice}</span>
                    <button aria-label="Fechar aviso" onClick={() => setNotice("")} type="button">×</button>
                </div>
            )}

            <Routes>
                <Route path="/login" element={<Login />} />
                <Route path="/cadastro" element={<Cadastro />} />

                <Route
                    element={
                        <ProtectedRoute>
                            <MainLayout />
                        </ProtectedRoute>
                    }
                >
                    <Route path="/cardapio" element={<Cardapio />} />
                    <Route
                        path="/carrinho"
                        element={
                            <CartPage
                                cart={cart}
                                onFinishOrder={finishOrder}
                                onUpdateQuantity={updateQuantity}
                            />
                        }
                    />
                    <Route path="/meus-pedidos" element={<OrdersPage orders={orders} />} />
                    <Route path="/cozinha" element={<Cozinha />} />
                    <Route path="/admin" element={<Admin />} />
                    <Route path="/admin/produtos" element={<AdminProdutos />} />
                    <Route path="/admin/usuarios" element={<AdminUsuarios />} />
                    <Route path="/admin-dashboard" element={<AdminDashboard />} />
                </Route>

                <Route path="*" element={<Login />} />
            </Routes>
        </div>
    );
}

function App() {
    return (
        <BrowserRouter>
            <AppRoutes />
        </BrowserRouter>
    );
}

export default App;
