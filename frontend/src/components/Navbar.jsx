import { NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/useAuth";
import { itensDoMenu } from "../utils/itensDoMenu";

export default function Navbar() {
  const { usuario, sair } = useAuth();
  const navigate = useNavigate();

  const menu = itensDoMenu(usuario?.role);

  function aoSair() {
    sair();
    navigate("/login", { replace: true });
  }

  return (
    <nav>
      <ul>
        {menu.map((item) => (
          <li key={item.path}>
            <NavLink to={item.path}>
              {item.label}
            </NavLink>
          </li>
        ))}
      </ul>

      {usuario && (
        <p>
          Olá, {usuario.nome}{" "}
          <button type="button" onClick={aoSair}>
            Sair
          </button>
        </p>
      )}
    </nav>
  );
}
