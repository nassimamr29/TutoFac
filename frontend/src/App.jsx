import {
  BrowserRouter,
  Navigate,
  Route,
  Routes,
} from "react-router-dom";

import { AuthProvider } from "./auth/AuthContext";
import Login from "./auth/Login";

import Layout from "./components/Layout";
import ProtectedRoute from "./components/ProtectedRoute";

import Accueil from "./pages/Accueil";

// Espace tuteur
import ProfilTuteur from "./pages/ProfilTuteur";
import Disponibilites from "./pages/Disponibilites";

// Espace gestionnaire
import ValidationTuteurs from "./pages/ValidationTuteurs";
import Besoin from "./pages/Besoin";
import Recherche from "./pages/Recherche";
import Affectation from "./pages/Affectation";

// Espace administrateur
import Administration from "./pages/Administration";

// Espace étudiant
import EspaceEtudiant from "./pages/EspaceEtudiant";

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route
            path="/connexion"
            element={<Login />}
          />

          <Route
            element={
              <ProtectedRoute>
                <Layout />
              </ProtectedRoute>
            }
          >
            <Route
              path="/accueil"
              element={<Accueil />}
            />

            {/* Espace tuteur */}
            <Route
              element={
                <ProtectedRoute
                  allowedRoles={["TUTEUR"]}
                />
              }
            >
              <Route
                path="/profil"
                element={<ProfilTuteur />}
              />

              <Route
                path="/disponibilites"
                element={<Disponibilites />}
              />
            </Route>

            {/* Espace gestionnaire */}
            <Route
              element={
                <ProtectedRoute
                  allowedRoles={["GESTIONNAIRE"]}
                />
              }
            >
              <Route
                path="/validation-tuteurs"
                element={<ValidationTuteurs />}
              />

              <Route
                path="/besoins"
                element={<Besoin />}
              />

              <Route
                path="/recherche"
                element={<Recherche />}
              />

              <Route
                path="/affectation"
                element={<Affectation />}
              />
            </Route>

            {/* Espace administrateur */}
            <Route
              element={
                <ProtectedRoute
                  allowedRoles={["ADMIN"]}
                />
              }
            >
              <Route
                path="/administration"
                element={<Administration />}
              />
            </Route>

            {/* Espace étudiant */}
            <Route
              element={
                <ProtectedRoute
                  allowedRoles={["ETUDIANT"]}
                />
              }
            >
              <Route
                path="/espace-etudiant"
                element={<EspaceEtudiant />}
              />
            </Route>
          </Route>

          <Route
            path="*"
            element={
              <Navigate
                to="/accueil"
                replace
              />
            }
          />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}