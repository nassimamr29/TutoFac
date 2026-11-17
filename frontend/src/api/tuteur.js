import { apiFetch, jsonOptions } from "./client";

export function getMonProfil() {
  return apiFetch("/api/tuteurs/me");
}

export function enregistrerProfil(profil) {
  return apiFetch("/api/tuteurs/me", jsonOptions("PUT", profil));
}

export function deposerReleve(fichier) {
  const formulaire = new FormData();
  formulaire.append("fichier", fichier);
  return apiFetch("/api/tuteurs/me/releve", {
    method: "POST",
    body: formulaire,
  });
}

export function listerMatieres() {
  return apiFetch("/api/matieres");
}