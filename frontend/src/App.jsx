import { BrowserRouter, Route, Routes } from "react-router-dom";
import MainLayout from "./layouts/MainLayout";
import ProtectedRoute from "./components/ProtectedRoute";
import { CarrinhoProvider } from "./contexts/CarrinhoContext";

import Login from "./pages/Login";
import Cadastro from "./pages/Cadastro";
import Cardapio from "./pages/Cardapio";
import Cozinha from "./pages/Cozinha";
import Admin from "./pages/Admin";
import AdminProdutos from "./pages/AdminProdutos";
import AdminUsuarios from "./pages/AdminUsuarios";
import AdminDashboard from "./pages/AdminDashboard";
import Carrinho from "./pages/Carrinho";
import MeusPedidos from "./pages/MeusPedidos";
import "./App.css";

function AppRoutes() {
    return (
        <div className="app-shell">
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
                    <Route path="/carrinho" element={<Carrinho />} />
                    <Route path="/meus-pedidos" element={<MeusPedidos />} />
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
