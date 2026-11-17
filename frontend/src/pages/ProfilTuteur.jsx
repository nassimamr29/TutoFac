import { useEffect, useRef, useState } from "react";
import { ApiError } from "../api/client";
import {
  deposerReleve,
  enregistrerProfil,
  getMonProfil,
  listerMatieres,
} from "../api/tuteur";

const NIVEAUX = ["L1", "L2", "L3", "M1", "M2"];
const PROFIL_VIDE = { formation: "", niveauEtudes: "L1", matiereIds: [] };

export default function ProfilTuteur() {
  const [profil, setProfil] = useState(null);
  const [matieres, setMatieres] = useState([]);
  const [formulaire, setFormulaire] = useState(PROFIL_VIDE);
  const [fichier, setFichier] = useState(null);
  const inputReleve = useRef(null);
  const [message, setMessage] = useState("");
  const [erreur, setErreur] = useState("");
  const [chargement, setChargement] = useState(true);

  useEffect(() => {
    async function charger() {
      try {
        const [listeMatieres, monProfil] = await Promise.all([
          listerMatieres(),
          getMonProfil().catch((error) => {
            if (error instanceof ApiError && error.status === 404) return null;
            throw error;
          }),
        ]);
        setMatieres(listeMatieres);
        if (monProfil) {
          setProfil(monProfil);
          setFormulaire({
            formation: monProfil.formation,
            niveauEtudes: monProfil.niveauEtudes,
            matiereIds: monProfil.matieres.map((matiere) => matiere.id),
          });
        }
      } catch (error) {
        setErreur(error.message);
      } finally {
        setChargement(false);
      }
    }
    charger();
  }, []);

  function mettreAJour(champ, valeur) {
    setFormulaire((actuel) => ({ ...actuel, [champ]: valeur }));
  }

  function basculerMatiere(id) {
    setFormulaire((actuel) => ({
      ...actuel,
      matiereIds: actuel.matiereIds.includes(id)
        ? actuel.matiereIds.filter((matiereId) => matiereId !== id)
        : [...actuel.matiereIds, id],
    }));
  }

  async function sauvegarder(event) {
    event.preventDefault();
    setErreur("");
    setMessage("");
    try {
      const resultat = await enregistrerProfil(formulaire);
      setProfil(resultat);
      setMessage("Profil enregistré. Il est en attente de validation.");
    } catch (error) {
      setErreur(error.message);
    }
  }

  async function envoyerReleve(event) {
    event.preventDefault();
    if (!fichier) return;
    setErreur("");
    setMessage("");
    try {
      const resultat = await deposerReleve(fichier);
      setProfil(resultat);
      setFichier(null);
      inputReleve.current.value = "";
      setMessage("Relevé envoyé. Le profil est en attente de validation.");
    } catch (error) {
      setErreur(error.message);
    }
  }

  if (chargement) return <main className="page"><p>Chargement du profil…</p></main>;

  return (
    <main className="page">
      <h1>Mon profil tuteur</h1>
      {profil && (
        <section className="status panel" aria-live="polite">
          <strong>Statut : {profil.statut.replace("_", " ")}</strong>
          {profil.motifRefus && <p>Motif du refus : {profil.motifRefus}</p>}
          <p>Relevé : {profil.relevePresent ? "déposé" : "à déposer"}</p>
        </section>
      )}
      {erreur && <p className="error" role="alert">{erreur}</p>}
      {message && <p className="success" role="status">{message}</p>}
      <form className="panel form-grid" onSubmit={sauvegarder}>
        <label>
          Formation
          <input
            maxLength="150"
            required
            value={formulaire.formation}
            onChange={(event) => mettreAJour("formation", event.target.value)}
          />
        </label>
        <label>
          Niveau d’études
          <select
            value={formulaire.niveauEtudes}
            onChange={(event) => mettreAJour("niveauEtudes", event.target.value)}
          >
            {NIVEAUX.map((niveau) => <option key={niveau}>{niveau}</option>)}
          </select>
        </label>
        <fieldset>
          <legend>Matières que vous pouvez enseigner</legend>
          {matieres.map((matiere) => (
            <label className="choice" key={matiere.id}>
              <input
                type="checkbox"
                checked={formulaire.matiereIds.includes(matiere.id)}
                onChange={() => basculerMatiere(matiere.id)}
              />
              {matiere.intitule} ({matiere.niveau}) — {matiere.domaine}
            </label>
          ))}
          {matieres.length === 0 && <p>Aucune matière n’est encore disponible.</p>}
        </fieldset>
        <button type="submit">Enregistrer mon profil</button>
      </form>

      <section className="panel">
        <h2>Relevé de notes</h2>
        <p>Fichier PDF uniquement, taille maximale : 5 Mo.</p>
        <form className="form-grid" onSubmit={envoyerReleve}>
          <label>
            Choisir un fichier PDF
            <input
              ref={inputReleve}
              type="file"
              accept="application/pdf,.pdf"
              required
              onChange={(event) => setFichier(event.target.files?.[0] ?? null)}
            />
          </label>
          <button disabled={!profil || !fichier}>Déposer le relevé</button>
          {!profil && <small>Enregistrez d’abord votre profil.</small>}
        </form>
      </section>
    </main>
  );
}