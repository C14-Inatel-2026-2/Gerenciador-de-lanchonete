import { NavLink } from "react-router-dom";
import { useAuth } from "../contexts/useAuth";
import { itensDoMenu } from "../utils/itensDoMenu";

export default function Navbar() {
  const { usuario } = useAuth();

  const menu = itensDoMenu(usuario?.role);

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
    </nav>
  );
}