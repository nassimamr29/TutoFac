import { Link } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

const CONFIGURATION = {
  GESTIONNAIRE: {
    badge: "ESPACE GESTIONNAIRE",
    titre: "Organisez le tutorat universitaire",
    description:
      "Gérez les besoins d'accompagnement, trouvez les tuteurs disponibles et planifiez les séances en quelques étapes.",
    cartes: [
      {
        numero: "01",
        titre: "Gestion des besoins",
        description:
          "Consultez les demandes de tutorat et créez de nouveaux besoins pour les étudiants.",
        lien: "/besoins",
        action: "Gérer les besoins",
      },
      {
        numero: "02",
        titre: "Recherche de tuteurs",
        description:
          "Identifiez les tuteurs correspondant à une matière et à un créneau horaire.",
        lien: "/recherche",
        action: "Rechercher un tuteur",
      },
      {
        numero: "03",
        titre: "Affectation des séances",
        description:
          "Organisez les séances de tutorat et consultez les affectations réalisées.",
        lien: "/affectation",
        action: "Organiser une séance",
      },
    ],
  },

  TUTEUR: {
    badge: "ESPACE TUTEUR",
    titre: "Partagez vos connaissances",
    description:
      "Complétez votre profil académique, gérez vos disponibilités et participez à l'accompagnement des étudiants.",
    cartes: [
      {
        numero: "01",
        titre: "Mon profil",
        description:
          "Renseignez votre formation, vos matières et consultez le statut de votre dossier.",
        lien: "/profil",
        action: "Accéder à mon profil",
      },
      {
        numero: "02",
        titre: "Mes disponibilités",
        description:
          "Ajoutez et gérez les créneaux pendant lesquels vous pouvez assurer du tutorat.",
        lien: "/disponibilites",
        action: "Gérer mon planning",
      },
    ],
  },

  ADMIN: {
    badge: "ADMINISTRATION",
    titre: "Bienvenue sur TutoFac",
    description:
      "Votre espace administrateur vous permet d'accéder à la plateforme.",
    cartes: [],
  },

  ETUDIANT: {
    badge: "ESPACE ÉTUDIANT",
    titre: "Bienvenue dans votre espace étudiant",
    description:
      "TutoFac facilite l'accompagnement universitaire entre étudiants et tuteurs.",
    cartes: [],
  },
};

export default function Accueil() {
  const { user } = useAuth();

  const contenu = CONFIGURATION[user?.role] ?? {
    badge: "TUTOFAC",
    titre: "Bienvenue sur TutoFac",
    description: "Votre plateforme de tutorat universitaire.",
    cartes: [],
  };

  return (
    <main className="dashboard">
      <section className="hero">
        <div className="hero-content">
          <div className="hero-badge">{contenu.badge}</div>

          <h1>{contenu.titre}</h1>

          <p>{contenu.description}</p>

          {contenu.cartes.length > 0 && (
            <Link
              className="hero-button"
              to={contenu.cartes[0].lien}
            >
              Commencer
              <span aria-hidden="true">→</span>
            </Link>
          )}
        </div>
      </section>

      <section className="dashboard-content">
        <div className="section-heading">
          <div>
            <span className="section-label">TUTOFAC</span>
            <h2>
              {contenu.cartes.length > 0
                ? "Que souhaitez-vous faire ?"
                : "Votre espace personnel"}
            </h2>
          </div>
        </div>

        {contenu.cartes.length > 0 ? (
          <div className="dashboard-grid">
            {contenu.cartes.map((carte) => (
              <Link
                key={carte.lien}
                className="dashboard-card"
                to={carte.lien}
              >
                <span className="card-number">
                  {carte.numero}
                </span>

                <h3>{carte.titre}</h3>

                <p>{carte.description}</p>

                <span className="card-action">
                  {carte.action}
                  <span aria-hidden="true">→</span>
                </span>
              </Link>
            ))}
          </div>
        ) : (
          <div className="dashboard-empty">
            <h3>Espace personnel</h3>
            <p>
              Les fonctionnalités complémentaires de cet espace
              ne sont pas encore disponibles dans cette version.
            </p>
          </div>
        )}
      </section>
    </main>
  );
}