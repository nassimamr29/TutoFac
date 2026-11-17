import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";

import { AuthProvider } from "./auth/AuthContext";
import Login from "./auth/Login";

import Layout from "./components/Layout";
import ProtectedRoute from "./components/ProtectedRoute";

import Accueil from "./pages/Accueil";
import Disponibilites from "./pages/Disponibilites";
import ProfilTuteur from "./pages/ProfilTuteur";

// Partie 3 : Besoins, recherche et affectation
import Besoin from "./pages/Besoin";
import Recherche from "./pages/Recherche";
import Affectation from "./pages/Affectation";

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>

          <Route path="/connexion" element={<Login />} />

          <Route
            element={
              <ProtectedRoute>
                <Layout />
              </ProtectedRoute>
            }
          >

            <Route path="/accueil" element={<Accueil />} />

            {/* Routes des tuteurs */}
            <Route
              element={
                <ProtectedRoute allowedRoles={["TUTEUR"]} />
              }
            >
              <Route path="/profil" element={<ProfilTuteur />} />
              <Route
                path="/disponibilites"
                element={<Disponibilites />}
              />
            </Route>

            {/* Partie 3 : Routes des gestionnaires */}
            <Route
              element={
                <ProtectedRoute allowedRoles={["GESTIONNAIRE"]} />
              }
            >
              <Route path="/besoins" element={<Besoin />} />
              <Route path="/recherche" element={<Recherche />} />
              <Route path="/affectation" element={<Affectation />} />
            </Route>

          </Route>

          <Route
            path="*"
            element={<Navigate to="/accueil" replace />}
          />

        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}