package fr.tutofac.besoin;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record BesoinRequest(

    @NotNull(message = "La matière est obligatoire")
    Long matiereId,

    @NotNull(message = "Le niveau est obligatoire")
    Besoin.Niveau niveau,

    @NotNull(message = "L'effectif est obligatoire")
    @Min(value = 1, message = "L'effectif doit être supérieur à 0")
    Integer effectif,

    @NotNull(message = "La date de début est obligatoire")
    LocalDate periodeDebut,

    @NotNull(message = "La date de fin est obligatoire")
    LocalDate periodeFin

) {}