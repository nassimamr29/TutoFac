import { useEffect, useState } from "react";
import { apiFetch } from "../api/client";

export default function EspaceEtudiant() {
  const [utilisateur, setUtilisateur] = useState(null);
  const [etablissements, setEtablissements] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState("");

  useEffect(() => {
    async function charger() {
      try {
        const [monCompte, listeEtablissements] = await Promise.all([
          apiFetch("/api/utilisateurs/me"),
          apiFetch("/api/etablissements"),
        ]);

        setUtilisateur(monCompte);
        setEtablissements(listeEtablissements);
      } catch (error) {
        setErreur(error.message);
      } finally {
        setChargement(false);
      }
    }

    charger();
  }, []);

  return (
    <main className="page">
      <h1>Mon espace étudiant</h1>

      <p>
        Retrouvez vos informations personnelles et découvrez
        les établissements partenaires de TutoFac.
      </p>

      {erreur && (
        <p className="error" role="alert">
          {erreur}
        </p>
      )}

      {chargement ? (
        <section className="panel">
          <p>Chargement de votre espace…</p>
        </section>
      ) : (
        <>
          <section className="panel">
            <h2>Mes informations</h2>

            {utilisateur ? (
              <>
                <p>
                  <strong>Nom :</strong>{" "}
                  {utilisateur.nom || "Non renseigné"}
                </p>

                <p>
                  <strong>Prénom :</strong>{" "}
                  {utilisateur.prenom || "Non renseigné"}
                </p>

                <p>
                  <strong>Adresse e-mail :</strong>{" "}
                  {utilisateur.email || "Non renseignée"}
                </p>

                <p>
                  <strong>Rôle :</strong>{" "}
                  {utilisateur.role || "ÉTUDIANT"}
                </p>

                {utilisateur.etablissementNom && (
                  <p>
                    <strong>Établissement :</strong>{" "}
                    {utilisateur.etablissementNom}
                  </p>
                )}
              </>
            ) : (
              <p>Informations indisponibles.</p>
            )}
          </section>

          <section className="panel">
            <h2>Établissements partenaires</h2>

            <p>
              TutoFac permet la mutualisation du tutorat
              entre plusieurs établissements universitaires.
            </p>

            {etablissements.length === 0 ? (
              <p>Aucun établissement enregistré.</p>
            ) : (
              <ul>
                {etablissements.map((etablissement) => (
                  <li key={etablissement.id}>
                    {etablissement.nom}
                  </li>
                ))}
              </ul>
            )}
          </section>

          <section className="panel">
            <h2>Accompagnement universitaire</h2>

            <p>
              Les demandes de tutorat et l'organisation
              des séances sont actuellement prises en charge
              par les gestionnaires d'établissement.
            </p>
          </section>
        </>
      )}
    </main>
  );
}