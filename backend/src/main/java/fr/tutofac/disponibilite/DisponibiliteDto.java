package fr.tutofac.disponibilite;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DisponibiliteDto(
        Long id,
        LocalDateTime debut,
        LocalDateTime fin,
        Recurrence recurrence,
        LocalDate finRecurrence
) {
    public static DisponibiliteDto from(Disponibilite disponibilite) {
        return new DisponibiliteDto(disponibilite.getId(), disponibilite.getDebut(),
                disponibilite.getFin(), disponibilite.getRecurrence(), disponibilite.getFinRecurrence());
    }
}
