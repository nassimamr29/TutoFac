package fr.tutofac.disponibilite;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record DisponibiliteRequest(
        @NotNull LocalDateTime debut,
        @NotNull LocalDateTime fin,
        @NotNull Recurrence recurrence,
        LocalDate finRecurrence
) {}
