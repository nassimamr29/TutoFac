package fr.tutofac.utilisateur;

public record UtilisateurDto(Long id, String nom, String prenom, String email, Role role,
                             Long etablissementId, String etablissementNom, boolean actif) {

    public static UtilisateurDto from(Utilisateur u) {
        var e = u.getEtablissement();
        return new UtilisateurDto(u.getId(), u.getNom(), u.getPrenom(), u.getEmail(), u.getRole(),
                e == null ? null : e.getId(), e == null ? null : e.getNom(), u.isActif());
    }
}