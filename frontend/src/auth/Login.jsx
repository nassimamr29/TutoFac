import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "./AuthContext";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [motDePasse, setMotDePasse] = useState("");
  const [erreur, setErreur] = useState("");
  const [chargement, setChargement] = useState(false);

  async function soumettre(event) {
    event.preventDefault();
    setErreur("");
    setChargement(true);
    try {
      const user = await login(email, motDePasse);
      navigate(user.role === "TUTEUR" ? "/profil" : "/accueil", { replace: true });
    } catch (error) {
      setErreur(error.message);
    } finally {
      setChargement(false);
    }
  }

  return (
    <main className="narrow-page">
      <h1>Connexion TutoFac</h1>
      <form className="panel form-grid" onSubmit={soumettre}>
        <label>
          Adresse e-mail
          <input
            type="email"
            autoComplete="username"
            required
            value={email}
            onChange={(event) => setEmail(event.target.value)}
          />
        </label>
        <label>
          Mot de passe
          <input
            type="password"
            autoComplete="current-password"
            required
            value={motDePasse}
            onChange={(event) => setMotDePasse(event.target.value)}
          />
        </label>
        {erreur && <p className="error" role="alert">{erreur}</p>}
        <button disabled={chargement}>
          {chargement ? "Connexion…" : "Se connecter"}
        </button>
      </form>
    </main>
  );
}