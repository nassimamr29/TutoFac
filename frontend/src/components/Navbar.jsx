import { Link } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function Navbar() {
  const { user, logout } = useAuth();

  return (
    <header className="navbar">
      <Link className="brand" to="/accueil">TutoFac</Link>

      {user && (
        <nav aria-label="Navigation principale">
          <Link to="/accueil">Accueil</Link>

          {user.role === "TUTEUR" && (
            <>
              <Link to="/profil">Mon profil</Link>
              <Link to="/disponibilites">Mes disponibilités</Link>
            </>
          )}

          {user.role === "GESTIONNAIRE" && (
            <>
              <Link to="/besoins">Gestion des besoins</Link>
              <Link to="/recherche">Recherche de tuteurs</Link>
              <Link to="/affectation">Affectation des séances</Link>
            </>
          )}

          <button className="secondary" onClick={logout}>
            Déconnexion
          </button>
        </nav>
      )}
    </header>
  );
}