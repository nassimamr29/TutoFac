import { Link } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

const CONFIGURATION = {
  GESTIONNAIRE: {
    badge: "ESPACE GESTIONNAIRE",
    titre: "Organisez le tutorat universitaire",
    description:
      "Validez les candidatures, gérez les besoins d'accompagnement, recherchez des tuteurs et planifiez les séances.",
    cartes: [
      {
        numero: "01",
        titre: "Validation des tuteurs",
        description:
          "Consultez les dossiers en attente, vérifiez les relevés de notes et prenez une décision.",
        lien: "/validation-tuteurs",
        action: "Consulter les candidatures",
      },
      {
        numero: "02",
        titre: "Gestion des besoins",
        description:
          "Créez les besoins de tutorat et suivez les demandes de votre établissement.",
        lien: "/besoins",
        action: "Gérer les besoins",
      },
      {
        numero: "03",
        titre: "Recherche de tuteurs",
        description:
          "Identifiez les tuteurs qualifiés et disponibles dans les établissements partenaires.",
        lien: "/recherche",
        action: "Rechercher un tuteur",
      },
      {
        numero: "04",
        titre: "Affectation des séances",
        description:
          "Planifiez les séances et consultez les affectations déjà réalisées.",
        lien: "/affectation",
        action: "Organiser une séance",
      },
    ],
  },

  TUTEUR: {
    badge: "ESPACE TUTEUR",
    titre: "Partagez vos connaissances",
    description:
      "Complétez votre dossier académique et organisez vos disponibilités pour accompagner les étudiants.",
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
          "Ajoutez, modifiez ou supprimez vos créneaux de tutorat.",
        lien: "/disponibilites",
        action: "Gérer mon planning",
      },
    ],
  },

  ADMIN: {
    badge: "ADMINISTRATION",
    titre: "Pilotez votre plateforme",
    description:
      "Retrouvez les utilisateurs et les établissements partenaires de TutoFac.",
    cartes: [
      {
        numero: "01",
        titre: "Administration",
        description:
          "Consultez les comptes utilisateurs et les établissements enregistrés.",
        lien: "/administration",
        action: "Accéder à l'administration",
      },
    ],
  },

  ETUDIANT: {
    badge: "ESPACE ÉTUDIANT",
    titre: "Bienvenue dans votre espace étudiant",
    description:
      "Retrouvez vos informations personnelles et découvrez le réseau universitaire TutoFac.",
    cartes: [
      {
        numero: "01",
        titre: "Mon espace étudiant",
        description:
          "Consultez votre profil et les établissements partenaires.",
        lien: "/espace-etudiant",
        action: "Accéder à mon espace",
      },
    ],
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
          <div className="hero-badge">
            {contenu.badge}
          </div>

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
            <span className="section-label">
              TUTOFAC
            </span>

            <h2>Que souhaitez-vous faire ?</h2>
          </div>
        </div>

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
      </section>
    </main>
  );
}