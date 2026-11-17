package fr.tutofac.tuteur;

import fr.tutofac.matiere.NiveauEtudes;

import java.util.Comparator;
import java.util.List;

public record ProfilTuteurDto(
        Long id,
        Long utilisateurId,
        String nom,
        String prenom,
        String email,
        String etablissementNom,
        String formation,
        NiveauEtudes niveauEtudes,
        StatutProfil statut,
        List<MatiereDto> matieres,
        boolean relevePresent,
        String motifRefus,
        Long valideParId
) {
    public record MatiereDto(Long id, String intitule, NiveauEtudes niveau, String domaine) {}

    public static ProfilTuteurDto from(ProfilTuteur profil) {
        var utilisateur = profil.getUtilisateur();
        return new ProfilTuteurDto(
                profil.getId(),
                utilisateur.getId(),
                utilisateur.getNom(),
                utilisateur.getPrenom(),
                utilisateur.getEmail(),
                utilisateur.getEtablissement() == null ? null : utilisateur.getEtablissement().getNom(),
                profil.getFormation(),
                profil.getNiveauEtudes(),
                profil.getStatut(),
                profil.getMatieres().stream()
                        .map(m -> new MatiereDto(m.getId(), m.getIntitule(), m.getNiveau(), m.getDomaine()))
                        .sorted(Comparator.comparing(MatiereDto::domaine)
                                .thenComparing(MatiereDto::intitule)
                                .thenComparing(MatiereDto::niveau))
                        .toList(),
                profil.getRelevePath() != null,
                profil.getMotifRefus(),
                profil.getValidePar() == null ? null : profil.getValidePar().getId());
    }
}
