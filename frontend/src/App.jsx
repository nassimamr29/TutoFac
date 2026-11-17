import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { AuthProvider } from "./auth/AuthContext";
import Login from "./auth/Login";
import Layout from "./components/Layout";
import ProtectedRoute from "./components/ProtectedRoute";
import Accueil from "./pages/Accueil";
import Disponibilites from "./pages/Disponibilites";
import ProfilTuteur from "./pages/ProfilTuteur";

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/connexion" element={<Login />} />
          <Route element={<ProtectedRoute><Layout /></ProtectedRoute>}>
            <Route path="/accueil" element={<Accueil />} />
            <Route element={<ProtectedRoute allowedRoles={["TUTEUR"]} />}>
              <Route path="/profil" element={<ProfilTuteur />} />
              <Route path="/disponibilites" element={<Disponibilites />} />
            </Route>
          </Route>
          <Route path="*" element={<Navigate to="/accueil" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}