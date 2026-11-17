import { apiFetch, jsonOptions } from "./client";

export function listerDisponibilites() {
  return apiFetch("/api/disponibilites");
}

export function creerDisponibilite(disponibilite) {
  return apiFetch("/api/disponibilites", jsonOptions("POST", disponibilite));
}

export function modifierDisponibilite(id, disponibilite) {
  return apiFetch(`/api/disponibilites/${id}`, jsonOptions("PUT", disponibilite));
}

export function supprimerDisponibilite(id) {
  return apiFetch(`/api/disponibilites/${id}`, { method: "DELETE" });
}