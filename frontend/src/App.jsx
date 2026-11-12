import { useRef, useState } from "react";
import { BrowserRouter, Route, Routes, useNavigate } from "react-router-dom";
import MainLayout from "./layouts/MainLayout";
import ProtectedRoute from "./components/ProtectedRoute";
import { CarrinhoProvider } from "./contexts/CarrinhoContext";
import { useCarrinho } from "./contexts/useCarrinho";
import { criarPedido } from "./services/pedidos";

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
import { mockOrders } from "./data/mockStore";
import "./App.css";

function AppRoutes() {
    const navigate = useNavigate();
    const { cart, total, atualizarQuantidade, limparCarrinho } = useCarrinho();
    const orders = mockOrders;
    const [notice, setNotice] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const checkoutInProgress = useRef(false);

    const finishOrder = async () => {
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
            navigate("/meus-pedidos");
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
        <div className="app-shell">
            {notice && (
                <div className="notice" role={notice.type === "error" ? "alert" : "status"}>
                    <span>{notice.message}</span>
                    <button aria-label="Fechar aviso" onClick={() => setNotice(null)} type="button">×</button>
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
                                total={total}
                                isSubmitting={isSubmitting}
                                onFinishOrder={finishOrder}
                                onUpdateQuantity={atualizarQuantidade}
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
            <CarrinhoProvider>
                <AppRoutes />
            </CarrinhoProvider>
        </BrowserRouter>
    );
}

export default App;
