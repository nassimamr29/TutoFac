import { useAuth } from "../auth/AuthContext";

const MESSAGES_PAR_ROLE = {
  ETUDIANT: {
    titre: "Espace étudiant",
    message: "Votre espace étudiant n’est pas encore disponible dans cette version.",
  },
  GESTIONNAIRE: {
    titre: "Espace gestionnaire",
    message: "L’interface de gestionnaire n’est pas encore disponible dans cette version.",
  },
  ADMIN: {
    titre: "Espace administrateur",
    message: "L’interface d’administration n’est pas encore disponible dans cette version.",
  },
};

export default function Accueil() {
  const { user } = useAuth();
  const contenu = MESSAGES_PAR_ROLE[user.role] ?? {
    titre: "Espace TutoFac",
    message: "Aucun espace n’est encore configuré pour ce compte.",
  };

  return (
    <main className="page">
      <h1>{contenu.titre}</h1>
      <section className="panel">
        <p>{contenu.message}</p>
      </section>
    </main>
  );
}
