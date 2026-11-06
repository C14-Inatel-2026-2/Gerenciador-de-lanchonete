import { BrowserRouter, Routes, Route } from "react-router-dom";

import Login from "./pages/Login";
import Cadastro from "./pages/Cadastro";
import Cardapio from "./pages/Cardapio";
import Carrinho from "./pages/Carrinho";
import MeusPedidos from "./pages/MeusPedidos";
import Cozinha from "./pages/Cozinha";
import Admin from "./pages/Admin";
import AdminProdutos from "./pages/AdminProdutos";
import AdminUsuarios from "./pages/AdminUsuarios";
import AdminDashboard from "./pages/AdminDashboard";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route path="/cadastro" element={<Cadastro />} />
        <Route path="/cardapio" element={<Cardapio />} />
        <Route path="/carrinho" element={<Carrinho />} />
        <Route path="/meus-pedidos" element={<MeusPedidos />} />
        <Route path="/cozinha" element={<Cozinha />} />
        <Route path="/admin" element={<Admin />} />
        <Route path="/admin/produtos" element={<AdminProdutos />} />
        <Route path="/admin/usuarios" element={<AdminUsuarios />} />
        <Route path="/admin-dashboard" element={<AdminDashboard />} />
        <Route path="*" element={<Login />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;