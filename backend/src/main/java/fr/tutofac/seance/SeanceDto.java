package fr.tutofac.seance;

import java.time.LocalDateTime;
import java.util.List;

public record SeanceDto(
        Long id,
        Long besoinId,
        Long profilTuteurId,
        LocalDateTime debut,
        LocalDateTime fin,
        String lieuOuLien,
        Seance.Statut statut,
        LocalDateTime createdAt,
        List<Long> etudiantIds
) {}