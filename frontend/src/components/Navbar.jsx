import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/useAuth";
import { itensDoMenu } from "../utils/itensDoMenu";
import "./Navbar.css";

export default function Navbar() {
  const { usuario, sair } = useAuth();
  const navigate = useNavigate();

  const menu = itensDoMenu(usuario?.role);

  function aoSair() {
    sair();
    navigate("/login", { replace: true });
  }

  return (
    <nav className="site-nav" aria-label="Navegação principal">
      <ul className="site-nav__links">
        {menu.map((item) => (
          <li key={item.path}>
            <NavLink to={item.path} end>
              {item.label}
            </NavLink>
          </li>
        ))}
      </ul>

      {usuario && (
        <div className="site-nav__user">
          <span>Olá, {usuario.nome}</span>
          <button className="site-nav__logout" type="button" onClick={aoSair}>
            Sair
          </button>
        </div>
      )}
    </nav>
  );
}
