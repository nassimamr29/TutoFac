package fr.tutofac.besoin;

public record TuteurRechercheDto(
    Long profilTuteurId,
    Long utilisateurId,
    String nom,
    String prenom,
    String formation,
    String niveauEtudes,
    Long etablissementId,
    String etablissementNom
) {}