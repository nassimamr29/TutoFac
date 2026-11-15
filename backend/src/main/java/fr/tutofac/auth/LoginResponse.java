package fr.tutofac.auth;

import fr.tutofac.utilisateur.Role;

public record LoginResponse(String token, Role role, Long etablissementId, String etablissementNom) {}