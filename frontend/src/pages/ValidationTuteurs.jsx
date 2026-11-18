import { useEffect, useState } from "react";
import { apiFetch, jsonOptions } from "../api/client";

export default function ValidationTuteurs() {
  const [profils, setProfils] = useState([]);
  const [motifs, setMotifs] = useState({});
  const [chargement, setChargement] = useState(true);
  const [traitement, setTraitement] = useState(null);
  const [erreur, setErreur] = useState("");
  const [message, setMessage] = useState("");

  async function chargerProfils() {
    setErreur("");

    try {
      const resultat = await apiFetch("/api/tuteurs/a-valider");
      setProfils(resultat);
    } catch (error) {
      setErreur(error.message);
    } finally {
      setChargement(false);
    }
  }

  useEffect(() => {
    chargerProfils();
  }, []);

  async function decider(profilId, decision) {
    const motifRefus = (motifs[profilId] || "").trim();

    if (decision === "REFUSE" && !motifRefus) {
      setErreur("Veuillez indiquer un motif de refus.");
      return;
    }

    setTraitement(profilId);
    setErreur("");
    setMessage("");

    try {
      await apiFetch(
        `/api/tuteurs/${profilId}/decision`,
        jsonOptions("POST", {
          decision,
          motifRefus: decision === "REFUSE" ? motifRefus : null,
        })
      );

      setMessage(
        decision === "VALIDE"
          ? "Le profil a été validé."
          : "Le profil a été refusé."
      );

      await chargerProfils();
    } catch (error) {
      setErreur(error.message);
    } finally {
      setTraitement(null);
    }
  }

  async function consulterReleve(profilId) {
    setErreur("");

    // Ouvrir l'onglet immédiatement évite son blocage
    // par certains navigateurs après une opération asynchrone.
    const nouvelOnglet = window.open("", "_blank");

    if (!nouvelOnglet) {
      setErreur("Autorisez les fenêtres contextuelles pour consulter le PDF.");
      return;
    }

    nouvelOnglet.document.title = "Chargement du relevé";
    nouvelOnglet.document.body.textContent = "Chargement du relevé PDF…";

    try {
      const baseUrl =
        import.meta.env.VITE_API_URL ?? "http://localhost:8080";

      const token = localStorage.getItem("token");

      const reponse = await fetch(
        `${baseUrl}/api/tuteurs/${profilId}/releve`,
        {
          headers: token
            ? { Authorization: `Bearer ${token}` }
            : {},
        }
      );

      if (!reponse.ok) {
        throw new Error(
          `Impossible de consulter le relevé (HTTP ${reponse.status}).`
        );
      }

      const fichier = await reponse.blob();
      const url = URL.createObjectURL(fichier);

      nouvelOnglet.location.href = url;

      // Conserver l'URL suffisamment longtemps pour que
      // le navigateur puisse afficher le PDF.
      setTimeout(() => URL.revokeObjectURL(url), 60000);
    } catch (error) {
      nouvelOnglet.close();
      setErreur(error.message);
    }
  }

  return (
    <main className="page">
      <h1>Validation des tuteurs</h1>

      <p>
        Consultez les candidatures en attente de votre établissement,
        vérifiez les informations académiques et prenez une décision.
      </p>

      {erreur && (
        <p className="error" role="alert">
          {erreur}
        </p>
      )}

      {message && (
        <p className="success" role="status">
          {message}
        </p>
      )}

      {chargement ? (
        <section className="panel">
          <p>Chargement des candidatures…</p>
        </section>
      ) : profils.length === 0 ? (
        <section className="panel">
          <h2>Aucune candidature en attente</h2>
          <p>
            Aucun dossier ne nécessite actuellement une décision
            de votre établissement.
          </p>
        </section>
      ) : (
        <div>
          <h2>Candidatures en attente ({profils.length})</h2>

          {profils.map((profil) => (
            <section className="panel" key={profil.id}>
              <h2>
                {profil.prenom} {profil.nom}
              </h2>

              <p>
                <strong>E-mail :</strong> {profil.email}
              </p>

              <p>
                <strong>Établissement :</strong>{" "}
                {profil.etablissementNom || "Non renseigné"}
              </p>

              <p>
                <strong>Formation :</strong>{" "}
                {profil.formation || "Non renseignée"}
              </p>

              <p>
                <strong>Niveau :</strong>{" "}
                {profil.niveauEtudes || "Non renseigné"}
              </p>

              <p>
                <strong>Statut :</strong> {profil.statut}
              </p>

              <h3>Matières proposées</h3>

              {profil.matieres?.length > 0 ? (
                <ul>
                  {profil.matieres.map((matiere) => (
                    <li key={matiere.id}>
                      {matiere.intitule} — {matiere.niveau}
                    </li>
                  ))}
                </ul>
              ) : (
                <p>Aucune matière renseignée.</p>
              )}

              <h3>Relevé de notes</h3>

              {profil.relevePresent ? (
                <button
                  type="button"
                  className="secondary"
                  onClick={() => consulterReleve(profil.id)}
                >
                  Consulter le relevé PDF
                </button>
              ) : (
                <p className="error">
                  Aucun relevé déposé. La validation est indisponible.
                </p>
              )}

              <hr
                style={{
                  border: 0,
                  borderTop: "1px solid #e4e9ef",
                  margin: "1.5rem 0",
                }}
              />

              <div className="actions">
                <button
                  type="button"
                  disabled={
                    traitement !== null || !profil.relevePresent
                  }
                  onClick={() => decider(profil.id, "VALIDE")}
                >
                  Valider le profil
                </button>
              </div>

              <div style={{ marginTop: "1.5rem" }}>
                <label>
                  Motif du refus
                  <textarea
                    maxLength={500}
                    placeholder="Précisez la raison du refus..."
                    value={motifs[profil.id] || ""}
                    onChange={(event) =>
                      setMotifs((actuels) => ({
                        ...actuels,
                        [profil.id]: event.target.value,
                      }))
                    }
                  />
                </label>

                <button
                  type="button"
                  className="danger"
                  style={{ marginTop: "0.75rem" }}
                  disabled={traitement !== null}
                  onClick={() => decider(profil.id, "REFUSE")}
                >
                  Refuser le profil
                </button>
              </div>
            </section>
          ))}
        </div>
      )}
    </main>
  );
}