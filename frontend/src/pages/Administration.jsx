import { useEffect, useState } from "react";
import { apiFetch } from "../api/client";

export default function Administration() {
  const [utilisateurs, setUtilisateurs] = useState([]);
  const [etablissements, setEtablissements] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState("");

  useEffect(() => {
    async function charger() {
      try {
        const [listeUtilisateurs, listeEtablissements] =
          await Promise.all([
            apiFetch("/api/utilisateurs"),
            apiFetch("/api/etablissements"),
          ]);

        setUtilisateurs(listeUtilisateurs);
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
      <h1>Administration de TutoFac</h1>

      <p>
        Consultez les utilisateurs et les établissements
        enregistrés sur la plateforme.
      </p>

      {erreur && (
        <p className="error" role="alert">
          {erreur}
        </p>
      )}

      {chargement ? (
        <section className="panel">
          <p>Chargement des données…</p>
        </section>
      ) : (
        <>
          <section className="panel">
            <h2>Vue d'ensemble</h2>

            <p>
              <strong>{utilisateurs.length}</strong>{" "}
              utilisateurs enregistrés
            </p>

            <p>
              <strong>{etablissements.length}</strong>{" "}
              établissements partenaires
            </p>
          </section>

          <section className="panel">
            <h2>Utilisateurs</h2>

            {utilisateurs.length === 0 ? (
              <p>Aucun utilisateur trouvé.</p>
            ) : (
              <div style={{ overflowX: "auto" }}>
                <table>
                  <thead>
                    <tr>
                      <th>Nom</th>
                      <th>Prénom</th>
                      <th>E-mail</th>
                      <th>Rôle</th>
                    </tr>
                  </thead>

                  <tbody>
                    {utilisateurs.map((utilisateur) => (
                      <tr key={utilisateur.id}>
                        <td>{utilisateur.nom || "—"}</td>
                        <td>{utilisateur.prenom || "—"}</td>
                        <td>{utilisateur.email || "—"}</td>
                        <td>{utilisateur.role || "—"}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>

          <section className="panel">
            <h2>Établissements partenaires</h2>

            {etablissements.length === 0 ? (
              <p>Aucun établissement trouvé.</p>
            ) : (
              <div style={{ overflowX: "auto" }}>
                <table>
                  <thead>
                    <tr>
                      <th>ID</th>
                      <th>Établissement</th>
                    </tr>
                  </thead>

                  <tbody>
                    {etablissements.map((etablissement) => (
                      <tr key={etablissement.id}>
                        <td>{etablissement.id}</td>
                        <td>
                          {etablissement.nom || "Non renseigné"}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </section>
        </>
      )}
    </main>
  );
}