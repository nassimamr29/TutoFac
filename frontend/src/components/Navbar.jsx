import { Link } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function Navbar() {
  const { user, logout } = useAuth();

  return (
    <header className="navbar">
      <Link className="brand" to="/accueil">TutoFac</Link>
      {user && (
        <nav aria-label="Navigation principale">
          {user.role === "TUTEUR" && (
            <>
              <Link to="/profil">Mon profil</Link>
              <Link to="/disponibilites">Mes disponibilités</Link>
            </>
          )}
          <button className="secondary" onClick={logout}>Déconnexion</button>
        </nav>
      )}
    </header>
  );
}