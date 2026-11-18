const API_BASE_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

export class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.name = "ApiError";
    this.status = status;
  }
}

export async function apiFetch(path, options = {}) {
  const headers = new Headers(options.headers);
  const token = localStorage.getItem("token");

  if (token) headers.set("Authorization", `Bearer ${token}`);

  const response = await fetch(`${API_BASE_URL}${path}`, {
    ...options,
    headers,
  });

  // 401 avec un token : session expirée, on renvoie vers la connexion
  if (response.status === 401 && token && !path.startsWith("/api/auth/")) {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    window.location.href = "/connexion";
    throw new ApiError("Session expirée, veuillez vous reconnecter", 401);
  }

  if (response.status === 204) return null;

  const contentType = response.headers.get("content-type") ?? "";
  const body = contentType.includes("application/json")
    ? await response.json()
    : await response.text();

  if (!response.ok) {
    const message =
      typeof body === "object" && body !== null && "message" in body
        ? body.message
        : `Erreur HTTP ${response.status}`;
    throw new ApiError(message, response.status);
  }

  return body;
}

export function jsonOptions(method, body) {
  return {
    method,
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(body),
  };
}