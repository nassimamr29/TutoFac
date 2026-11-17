import { useEffect, useState } from "react";
import {
  creerDisponibilite,
  listerDisponibilites,
  modifierDisponibilite,
  supprimerDisponibilite,
} from "../api/disponibilite";

const FORMULAIRE_VIDE = {
  debut: "",
  fin: "",
  recurrence: "AUCUNE",
  finRecurrence: "",
};

function versLocalDateTime(value) {
  return value ? value.slice(0, 16) : "";
}

function versPayload(formulaire) {
  return {
    ...formulaire,
    debut: formulaire.debut,
    fin: formulaire.fin,
    finRecurrence: formulaire.recurrence === "HEBDOMADAIRE"
      ? formulaire.finRecurrence || null
      : null,
  };
}

export default function Disponibilites() {
  const [disponibilites, setDisponibilites] = useState([]);
  const [formulaire, setFormulaire] = useState(FORMULAIRE_VIDE);
  const [editionId, setEditionId] = useState(null);
  const [erreur, setErreur] = useState("");
  const [message, setMessage] = useState("");

  async function charger() {
    try {
      setDisponibilites(await listerDisponibilites());
    } catch (error) {
      setErreur(error.message);
    }
  }

  useEffect(() => { charger(); }, []);

  function modifierChamp(champ, valeur) {
    setFormulaire((actuel) => ({ ...actuel, [champ]: valeur }));
  }

  function commencerEdition(disponibilite) {
    setEditionId(disponibilite.id);
    setFormulaire({
      debut: versLocalDateTime(disponibilite.debut),
      fin: versLocalDateTime(disponibilite.fin),
      recurrence: disponibilite.recurrence,
      finRecurrence: disponibilite.finRecurrence ?? "",
    });
    setErreur("");
    setMessage("");
  }

  function annulerEdition() {
    setEditionId(null);
    setFormulaire(FORMULAIRE_VIDE);
  }

  async function soumettre(event) {
    event.preventDefault();
    setErreur("");
    setMessage("");
    try {
      const payload = versPayload(formulaire);
      if (editionId === null) await creerDisponibilite(payload);
      else await modifierDisponibilite(editionId, payload);
      annulerEdition();
      setMessage("Disponibilité enregistrée.");
      await charger();
    } catch (error) {
      setErreur(error.message);
    }
  }

  async function supprimer(id) {
    setErreur("");
    setMessage("");
    try {
      await supprimerDisponibilite(id);
      setDisponibilites((actuel) => actuel.filter((item) => item.id !== id));
      if (editionId === id) annulerEdition();
      setMessage("Disponibilité supprimée.");
    } catch (error) {
      setErreur(error.message);
    }
  }

  return (
    <main className="page">
      <h1>Mes disponibilités</h1>
      {erreur && <p className="error" role="alert">{erreur}</p>}
      {message && <p className="success" role="status">{message}</p>}
      <form className="panel form-grid" onSubmit={soumettre}>
        <h2>{editionId === null ? "Ajouter un créneau" : "Modifier le créneau"}</h2>
        <label>
          Début
          <input
            type="datetime-local"
            required
            value={formulaire.debut}
            onChange={(event) => modifierChamp("debut", event.target.value)}
          />
        </label>
        <label>
          Fin
          <input
            type="datetime-local"
            required
            value={formulaire.fin}
            onChange={(event) => modifierChamp("fin", event.target.value)}
          />
        </label>
        <label>
          Récurrence
          <select
            value={formulaire.recurrence}
            onChange={(event) => modifierChamp("recurrence", event.target.value)}
          >
            <option value="AUCUNE">Ponctuelle</option>
            <option value="HEBDOMADAIRE">Chaque semaine</option>
          </select>
        </label>
        {formulaire.recurrence === "HEBDOMADAIRE" && (
          <label>
            Répéter jusqu’au
            <input
              type="date"
              required
              value={formulaire.finRecurrence}
              onChange={(event) => modifierChamp("finRecurrence", event.target.value)}
            />
          </label>
        )}
        <div className="actions">
          <button type="submit">{editionId === null ? "Ajouter" : "Enregistrer"}</button>
          {editionId !== null && (
            <button type="button" className="secondary" onClick={annulerEdition}>
              Annuler
            </button>
          )}
        </div>
      </form>

      <section className="slot-list" aria-label="Créneaux existants">
        {disponibilites.map((item) => (
          <article className="panel slot" key={item.id}>
            <div>
              <strong>{new Date(item.debut).toLocaleString()}</strong>
              {" — "}
              {new Date(item.fin).toLocaleTimeString()}
              <p>
                {item.recurrence === "HEBDOMADAIRE"
                  ? `Chaque semaine jusqu’au ${item.finRecurrence}`
                  : "Créneau ponctuel"}
              </p>
            </div>
            <div className="actions">
              <button className="secondary" onClick={() => commencerEdition(item)}>
                Modifier
              </button>
              <button className="danger" onClick={() => supprimer(item.id)}>
                Supprimer
              </button>
            </div>
          </article>
        ))}
        {disponibilites.length === 0 && (
          <p>Aucun créneau enregistré pour le moment.</p>
        )}
      </section>
    </main>
  );
}