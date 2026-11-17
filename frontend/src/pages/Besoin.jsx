import { useEffect, useState } from "react";
import {
  listerBesoins,
  creerBesoin,
  annulerBesoin,
} from "../api/besoin";

const formulaireInitial = {
  matiereId: "",
  niveau: "L1",
  effectif: 1,
  periodeDebut: "",
  periodeFin: "",
};

export default function Besoin() {
  const [besoins, setBesoins] = useState([]);
  const [formulaire, setFormulaire] = useState(formulaireInitial);
  const [chargement, setChargement] = useState(true);
  const [envoi, setEnvoi] = useState(false);
  const [erreur, setErreur] = useState("");
  const [succes, setSucces] = useState("");

  async function chargerBesoins() {
    try {
      const resultat = await listerBesoins();
      setBesoins(resultat);
      setErreur("");
    } catch (error) {
      setErreur(error.message);
    } finally {
      setChargement(false);
    }
  }

  useEffect(() => {
    chargerBesoins();
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

    if (formulaire.periodeFin < formulaire.periodeDebut) {
      setErreur("La date de fin doit être après la date de début.");
      return;
    }

    setEnvoi(true);

    try {
      await creerBesoin({
        matiereId: Number(formulaire.matiereId),
        niveau: formulaire.niveau,
        effectif: Number(formulaire.effectif),
        periodeDebut: formulaire.periodeDebut,
        periodeFin: formulaire.periodeFin,
      });

      setSucces("Le besoin de tutorat a été créé.");
      setFormulaire(formulaireInitial);

      await chargerBesoins();
    } catch (error) {
      setErreur(error.message);
    } finally {
      setEnvoi(false);
    }
  }

  async function annuler(id) {
    if (!window.confirm("Voulez-vous annuler ce besoin ?")) {
      return;
    }

    setErreur("");
    setSucces("");

    try {
      await annulerBesoin(id);
      setSucces("Le besoin a été annulé.");
      await chargerBesoins();
    } catch (error) {
      setErreur(error.message);
    }
  }

  return (
    <main style={{ padding: "24px" }}>
      <h1>Gestion des besoins de tutorat</h1>

      <p>
        Créez des demandes de tutorat pour votre établissement
        et suivez leur état.
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

      <section style={{ marginTop: "30px" }}>
        <h2>Créer un besoin</h2>

        <form
          onSubmit={soumettre}
          style={{
            display: "grid",
            gap: "14px",
            maxWidth: "450px",
          }}
        >
          <label>
            Identifiant de la matière
            <input
              type="number"
              name="matiereId"
              min="1"
              required
              value={formulaire.matiereId}
              onChange={modifierChamp}
              style={{ display: "block", width: "100%" }}
            />
          </label>

          <label>
            Niveau
            <select
              name="niveau"
              value={formulaire.niveau}
              onChange={modifierChamp}
              style={{ display: "block", width: "100%" }}
            >
              <option value="L1">Licence 1</option>
              <option value="L2">Licence 2</option>
              <option value="L3">Licence 3</option>
              <option value="M1">Master 1</option>
              <option value="M2">Master 2</option>
            </select>
          </label>

          <label>
            Nombre d'étudiants
            <input
              type="number"
              name="effectif"
              min="1"
              required
              value={formulaire.effectif}
              onChange={modifierChamp}
              style={{ display: "block", width: "100%" }}
            />
          </label>

          <label>
            Début de la période
            <input
              type="date"
              name="periodeDebut"
              required
              value={formulaire.periodeDebut}
              onChange={modifierChamp}
              style={{ display: "block", width: "100%" }}
            />
          </label>

          <label>
            Fin de la période
            <input
              type="date"
              name="periodeFin"
              required
              value={formulaire.periodeFin}
              onChange={modifierChamp}
              style={{ display: "block", width: "100%" }}
            />
          </label>

          <button type="submit" disabled={envoi}>
            {envoi ? "Création..." : "Créer le besoin"}
          </button>
        </form>
      </section>

      <section style={{ marginTop: "40px" }}>
        <h2>Besoins de mon établissement</h2>

        {chargement ? (
          <p>Chargement des besoins...</p>
        ) : besoins.length === 0 ? (
          <p>Aucun besoin enregistré.</p>
        ) : (
          <div style={{ overflowX: "auto" }}>
            <table
              border="1"
              cellPadding="10"
              style={{ borderCollapse: "collapse", width: "100%" }}
            >
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Matière</th>
                  <th>Niveau</th>
                  <th>Effectif</th>
                  <th>Début</th>
                  <th>Fin</th>
                  <th>Statut</th>
                  <th>Action</th>
                </tr>
              </thead>

              <tbody>
                {besoins.map((besoin) => (
                  <tr key={besoin.id}>
                    <td>{besoin.id}</td>
                    <td>{besoin.matiereId}</td>
                    <td>{besoin.niveau}</td>
                    <td>{besoin.effectif}</td>
                    <td>{besoin.periodeDebut}</td>
                    <td>{besoin.periodeFin}</td>
                    <td>{besoin.statut}</td>
                    <td>
                      {besoin.statut !== "ANNULE" ? (
                        <button
                          type="button"
                          onClick={() => annuler(besoin.id)}
                        >
                          Annuler
                        </button>
                      ) : (
                        "—"
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