import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { listerBesoins } from "../api/besoin";
import {
  creerSeance,
  listerSeances,
  annulerSeance,
  realiserSeance,
} from "../api/seance";

export default function Affectation() {
  const [params] = useSearchParams();

  const [besoins, setBesoins] = useState([]);
  const [seances, setSeances] = useState([]);
  const [erreur, setErreur] = useState("");
  const [succes, setSucces] = useState("");
  const [chargement, setChargement] = useState(false);

  const [formulaire, setFormulaire] = useState({
    besoinId: params.get("besoinId") || "",
    profilTuteurId: params.get("profilTuteurId") || "",
    debut: params.get("debut") || "",
    fin: params.get("fin") || "",
    lieuOuLien: "",
    etudiantIds: "",
  });

  async function chargerDonnees() {
    try {
      const [listeBesoins, listeSeances] = await Promise.all([
        listerBesoins(),
        listerSeances(),
      ]);

      setBesoins(listeBesoins);
      setSeances(listeSeances);
    } catch (error) {
      setErreur(error.message);
    }
  }

  useEffect(() => {
    chargerDonnees();
  }, []);

  function modifierChamp(event) {
    const { name, value } = event.target;

    setFormulaire((precedent) => ({
      ...precedent,
      [name]: value,
    }));
  }

  async function soumettre(event) {
    event.preventDefault();

    setErreur("");
    setSucces("");

    if (formulaire.fin <= formulaire.debut) {
      setErreur("La fin doit être après le début.");
      return;
    }

    const etudiantIds = formulaire.etudiantIds
      .split(",")
      .map((id) => id.trim())
      .filter(Boolean)
      .map(Number);

    if (
      etudiantIds.length === 0 ||
      etudiantIds.some((id) => !Number.isInteger(id) || id <= 0)
    ) {
      setErreur(
        "Indiquez au moins un identifiant étudiant valide."
      );
      return;
    }

    setChargement(true);

    try {
      await creerSeance({
        besoinId: Number(formulaire.besoinId),
        profilTuteurId: Number(formulaire.profilTuteurId),
        debut: formulaire.debut,
        fin: formulaire.fin,
        lieuOuLien: formulaire.lieuOuLien,
        etudiantIds,
      });

      setSucces("La séance a été créée avec succès.");

      await chargerDonnees();
    } catch (error) {
      setErreur(error.message);
    } finally {
      setChargement(false);
    }
  }

  async function changerStatut(id, action) {
    setErreur("");
    setSucces("");

    try {
      if (action === "annuler") {
        await annulerSeance(id);
        setSucces("La séance a été annulée.");
      } else {
        await realiserSeance(id);
        setSucces("La séance est marquée comme réalisée.");
      }

      await chargerDonnees();
    } catch (error) {
      setErreur(error.message);
    }
  }

  return (
    <main style={{ padding: 24 }}>
      <h1>Affectation des séances</h1>

      <p>
        Affectez un tuteur et des étudiants à un besoin de tutorat.
      </p>

      {erreur && (
        <p role="alert" style={{ color: "crimson" }}>
          {erreur}
        </p>
      )}

      {succes && (
        <p role="status" style={{ color: "green" }}>
          {succes}
        </p>
      )}

      <section style={{ marginTop: 24 }}>
        <h2>Planifier une séance</h2>

        <form
          onSubmit={soumettre}
          style={{
            display: "grid",
            gap: 14,
            maxWidth: 500,
          }}
        >
          <label>
            Besoin
            <select
              name="besoinId"
              required
              value={formulaire.besoinId}
              onChange={modifierChamp}
              style={{ display: "block", width: "100%" }}
            >
              <option value="">Sélectionner un besoin</option>

              {besoins
                .filter((b) => b.statut !== "ANNULE")
                .map((besoin) => (
                  <option key={besoin.id} value={besoin.id}>
                    Besoin #{besoin.id} — Matière {besoin.matiereId}
                  </option>
                ))}
            </select>
          </label>

          <label>
            Identifiant du profil tuteur
            <input
              type="number"
              min="1"
              name="profilTuteurId"
              required
              value={formulaire.profilTuteurId}
              onChange={modifierChamp}
              style={{ display: "block", width: "100%" }}
            />
          </label>

          <label>
            Début
            <input
              type="datetime-local"
              name="debut"
              required
              value={formulaire.debut}
              onChange={modifierChamp}
              style={{ display: "block", width: "100%" }}
            />
          </label>

          <label>
            Fin
            <input
              type="datetime-local"
              name="fin"
              required
              value={formulaire.fin}
              onChange={modifierChamp}
              style={{ display: "block", width: "100%" }}
            />
          </label>

          <label>
            Salle ou lien de visioconférence
            <input
              type="text"
              name="lieuOuLien"
              value={formulaire.lieuOuLien}
              onChange={modifierChamp}
              placeholder="Salle B204"
              style={{ display: "block", width: "100%" }}
            />
          </label>

          <label>
            Identifiants des étudiants
            <input
              type="text"
              name="etudiantIds"
              required
              value={formulaire.etudiantIds}
              onChange={modifierChamp}
              placeholder="Exemple : 1, 5, 6"
              style={{ display: "block", width: "100%" }}
            />
            <small>
              Séparez les identifiants par des virgules.
            </small>
          </label>

          <button type="submit" disabled={chargement}>
            {chargement ? "Création..." : "Créer la séance"}
          </button>
        </form>
      </section>

      <section style={{ marginTop: 40 }}>
        <h2>Séances de mon établissement</h2>

        {seances.length === 0 ? (
          <p>Aucune séance enregistrée.</p>
        ) : (
          <div style={{ overflowX: "auto" }}>
            <table
              border="1"
              cellPadding="10"
              style={{
                borderCollapse: "collapse",
                width: "100%",
              }}
            >
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Besoin</th>
                  <th>Tuteur</th>
                  <th>Début</th>
                  <th>Fin</th>
                  <th>Lieu</th>
                  <th>Étudiants</th>
                  <th>Statut</th>
                  <th>Actions</th>
                </tr>
              </thead>

              <tbody>
                {seances.map((seance) => (
                  <tr key={seance.id}>
                    <td>{seance.id}</td>
                    <td>{seance.besoinId}</td>
                    <td>{seance.profilTuteurId}</td>
                    <td>{seance.debut.replace("T", " ")}</td>
                    <td>{seance.fin.replace("T", " ")}</td>
                    <td>{seance.lieuOuLien || "—"}</td>
                    <td>{seance.etudiantIds.join(", ")}</td>
                    <td>{seance.statut}</td>
                    <td>
                      {seance.statut === "PLANIFIEE" && (
                        <>
                          <button
                            type="button"
                            onClick={() =>
                              changerStatut(seance.id, "annuler")
                            }
                          >
                            Annuler
                          </button>{" "}
                          <button
                            type="button"
                            onClick={() =>
                              changerStatut(seance.id, "realiser")
                            }
                          >
                            Réalisée
                          </button>
                        </>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>
    </main>
  );
}