package fr.tutofac.besoin;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record BesoinDto(
    Long id,
    Long etablissementId,
    Long matiereId,
    Long creePar,
    Besoin.Niveau niveau,
    Integer effectif,
    LocalDate periodeDebut,
    LocalDate periodeFin,
    Besoin.Statut statut,
    LocalDateTime createdAt
) {

    public static BesoinDto from(Besoin besoin) {
        return new BesoinDto(
            besoin.getId(),
            besoin.getEtablissementId(),
            besoin.getMatiereId(),
            besoin.getCreePar(),
            besoin.getNiveau(),
            besoin.getEffectif(),
            besoin.getPeriodeDebut(),
            besoin.getPeriodeFin(),
            besoin.getStatut(),
            besoin.getCreatedAt()
        );
    }
}