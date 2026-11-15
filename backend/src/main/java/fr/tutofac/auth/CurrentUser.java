package fr.tutofac.auth;

import fr.tutofac.utilisateur.Role;
import fr.tutofac.utilisateur.Utilisateur;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

public final class CurrentUser {

    private CurrentUser() {}

    public static Utilisateur get() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !(a.getPrincipal() instanceof Utilisateur u)) {
            throw new AccessDeniedException("Non authentifié");
        }
        return u;
    }

    public static boolean isAdmin() {
        return get().getRole() == Role.ADMIN;
    }

    /** null pour un ADMIN. */
    public static Long etablissementId() {
        var e = get().getEtablissement();
        return e == null ? null : e.getId();
    }

    /** Lève une 403 si la ressource appartient à un autre établissement (sauf ADMIN). */
    public static void checkEtablissement(Long etablissementId) {
        if (isAdmin()) return;
        if (!Objects.equals(etablissementId(), etablissementId)) {
            throw new AccessDeniedException("Ressource d'un autre établissement");
        }
    }
}