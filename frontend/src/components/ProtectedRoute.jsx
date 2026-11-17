import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

export default function ProtectedRoute({ children, allowedRoles }) {
  const { user } = useAuth();
  if (!user) return <Navigate to="/connexion" replace />;
  if (allowedRoles && !allowedRoles.includes(user.role)) {
    return <Navigate to="/accueil" replace />;
  }
  return children ?? <Outlet />;
}