import { apiFetch, jsonOptions } from "./client";

export function listerSeances() {
  return apiFetch("/api/seances");
}

export function obtenirSeance(id) {
  return apiFetch(`/api/seances/${id}`);
}

export function listerSeancesParBesoin(besoinId) {
  return apiFetch(`/api/seances/besoin/${besoinId}`);
}

export function creerSeance(donnees) {
  return apiFetch("/api/seances", jsonOptions("POST", donnees));
}

export function annulerSeance(id) {
  return apiFetch(`/api/seances/${id}/annuler`, {
    method: "PATCH",
  });
}

export function realiserSeance(id) {
  return apiFetch(`/api/seances/${id}/realiser`, {
    method: "PATCH",
  });
}