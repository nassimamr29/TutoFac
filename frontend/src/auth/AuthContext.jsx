import { createContext, useContext, useMemo, useState } from "react";
import { apiFetch, jsonOptions } from "../api/client";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem("user");
    return stored ? JSON.parse(stored) : null;
  });

  async function login(email, motDePasse) {
    const result = await apiFetch(
      "/api/auth/login",
      jsonOptions("POST", { email, motDePasse }),
    );
    localStorage.setItem("token", result.token);
    const nextUser = { role: result.role };
    localStorage.setItem("user", JSON.stringify(nextUser));
    setUser(nextUser);
    return nextUser;
  }

  function logout() {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setUser(null);
  }

  const value = useMemo(() => ({ user, login, logout }), [user]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth doit être utilisé dans AuthProvider");
  return context;
}