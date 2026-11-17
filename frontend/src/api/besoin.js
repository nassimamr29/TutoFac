import { apiFetch, jsonOptions } from "./client";

export function listerBesoins() {
  return apiFetch("/api/besoins");
}

export function obtenirBesoin(id) {
  return apiFetch(`/api/besoins/${id}`);
}

export function creerBesoin(donnees) {
  return apiFetch("/api/besoins", jsonOptions("POST", donnees));
}

export function annulerBesoin(id) {
  return apiFetch(`/api/besoins/${id}/annuler`, {
    method: "PATCH",
  });
}

export function rechercherTuteurs(besoinId, debut, fin) {
  const params = new URLSearchParams({
    debut,
    fin,
  });

  return apiFetch(`/api/besoins/${besoinId}/tuteurs?${params}`);
}