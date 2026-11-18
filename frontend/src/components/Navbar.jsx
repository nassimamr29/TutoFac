import { Link, NavLink } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

const LIENS_PAR_ROLE = {
  TUTEUR: [
    { label: "Mon profil", chemin: "/profil" },
    { label: "Mes disponibilités", chemin: "/disponibilites" },
  ],

  GESTIONNAIRE: [
    { label: "Validation des tuteurs", chemin: "/validation-tuteurs" },
    { label: "Gestion des besoins", chemin: "/besoins" },
    { label: "Recherche de tuteurs", chemin: "/recherche" },
    { label: "Affectation", chemin: "/affectation" },
  ],

  ADMIN: [
    { label: "Administration", chemin: "/administration" },
  ],

  ETUDIANT: [
    { label: "Mon espace", chemin: "/espace-etudiant" },
  ],
};

export default function Navbar() {
  const { user, logout } = useAuth();

  const liens = user
    ? LIENS_PAR_ROLE[user.role] ?? []
    : [];

  return (
    <header className="navbar">
      <Link className="brand" to="/accueil">
        Tuto<span>Fac</span>
      </Link>

      {user && (
        <nav aria-label="Navigation principale">
          <NavLink
            to="/accueil"
            className={({ isActive }) =>
              isActive ? "nav-link active" : "nav-link"
            }
          >
            Accueil
          </NavLink>

          {liens.map((lien) => (
            <NavLink
              key={lien.chemin}
              to={lien.chemin}
              className={({ isActive }) =>
                isActive ? "nav-link active" : "nav-link"
              }
            >
              {lien.label}
            </NavLink>
          ))}

          <button
            type="button"
            className="secondary logout-button"
            onClick={logout}
          >
            Déconnexion
          </button>
        </nav>
      )}
    </header>
  );
}