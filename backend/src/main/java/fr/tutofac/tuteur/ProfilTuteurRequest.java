package fr.tutofac.tuteur;

import fr.tutofac.matiere.NiveauEtudes;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ProfilTuteurRequest(
        @NotBlank @Size(max = 150) String formation,
        @NotNull NiveauEtudes niveauEtudes,
        @NotEmpty List<@NotNull Long> matiereIds
) {}
