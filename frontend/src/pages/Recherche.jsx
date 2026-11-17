import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { listerBesoins, rechercherTuteurs } from "../api/besoin";

export default function Recherche() {
  const [params] = useSearchParams();

  const [besoins, setBesoins] = useState([]);
  const [besoinId, setBesoinId] = useState(
    params.get("besoinId") || ""
  );
  const [debut, setDebut] = useState("");
  const [fin, setFin] = useState("");
  const [tuteurs, setTuteurs] = useState([]);
  const [erreur, setErreur] = useState("");
  const [chargement, setChargement] = useState(false);
  const [rechercheEffectuee, setRechercheEffectuee] = useState(false);

  useEffect(() => {
    async function charger() {
      try {
        const resultat = await listerBesoins();
        setBesoins(resultat.filter((b) => b.statut !== "ANNULE"));
      } catch (error) {
        setErreur(error.message);
      }
    }

    charger();
  }, []);

  async function rechercher(event) {
    event.preventDefault();

    setErreur("");
    setTuteurs([]);
    setRechercheEffectuee(false);

    if (!besoinId || !debut || !fin) {
      setErreur("Tous les champs sont obligatoires.");
      return;
    }

    if (fin <= debut) {
      setErreur("La fin doit être après le début.");
      return;
    }

    setChargement(true);

    try {
      const resultat = await rechercherTuteurs(besoinId, debut, fin);
      setTuteurs(resultat);
      setRechercheEffectuee(true);
    } catch (error) {
      setErreur(error.message);
    } finally {
      setChargement(false);
    }
  }

  return (
    <main style={{ padding: 24 }}>
      <h1>Recherche de tuteurs</h1>

      <p>
        Recherchez les tuteurs qualifiés et disponibles,
        y compris dans les autres établissements.
      </p>

      {erreur && (
        <p role="alert" style={{ color: "crimson" }}>
          {erreur}
        </p>
      )}

      <form
        onSubmit={rechercher}
        style={{
          display: "grid",
          gap: 14,
          maxWidth: 500,
          marginTop: 24,
        }}
      >
        <label>
          Besoin de tutorat
          <select
            required
            value={besoinId}
            onChange={(e) => setBesoinId(e.target.value)}
            style={{ display: "block", width: "100%" }}
          >
            <option value="">Sélectionner un besoin</option>

            {besoins.map((besoin) => (
              <option key={besoin.id} value={besoin.id}>
                Besoin #{besoin.id} — Matière {besoin.matiereId}
                {" — "}{besoin.niveau}
              </option>
            ))}
          </select>
        </label>

        <label>
          Début de la séance
          <input
            type="datetime-local"
            required
            value={debut}
            onChange={(e) => setDebut(e.target.value)}
            style={{ display: "block", width: "100%" }}
          />
        </label>

        <label>
          Fin de la séance
          <input
            type="datetime-local"
            required
            value={fin}
            onChange={(e) => setFin(e.target.value)}
            style={{ display: "block", width: "100%" }}
          />
        </label>

        <button type="submit" disabled={chargement}>
          {chargement ? "Recherche en cours..." : "Rechercher"}
        </button>
      </form>

      {rechercheEffectuee && (
        <section style={{ marginTop: 32 }}>
          <h2>Résultats ({tuteurs.length})</h2>

          {tuteurs.length === 0 ? (
            <p>Aucun tuteur disponible sur ce créneau.</p>
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
                    <th>Nom</th>
                    <th>Formation</th>
                    <th>Niveau</th>
                    <th>Établissement</th>
                    <th>Action</th>
                  </tr>
                </thead>

                <tbody>
                  {tuteurs.map((tuteur) => {
                    const query = new URLSearchParams({
                      besoinId: String(besoinId),
                      profilTuteurId: String(tuteur.profilTuteurId),
                      debut,
                      fin,
                    });

                    return (
                      <tr key={tuteur.profilTuteurId}>
                        <td>
                          {tuteur.prenom} {tuteur.nom}
                        </td>
                        <td>{tuteur.formation}</td>
                        <td>{tuteur.niveauEtudes}</td>
                        <td>{tuteur.etablissementNom}</td>
                        <td>
                          <Link to={`/affectation?${query}`}>
                            Affecter
                          </Link>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </section>
      )}
    </main>
  );
}