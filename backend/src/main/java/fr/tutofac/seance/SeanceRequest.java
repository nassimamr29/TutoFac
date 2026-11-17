package fr.tutofac.seance;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record SeanceRequest(

        @NotNull(message = "Le besoin est obligatoire")
        Long besoinId,

        @NotNull(message = "Le tuteur est obligatoire")
        Long profilTuteurId,

        @NotNull(message = "La date de début est obligatoire")
        LocalDateTime debut,

        @NotNull(message = "La date de fin est obligatoire")
        LocalDateTime fin,

        @Size(max = 255, message = "Le lieu ou lien ne peut pas dépasser 255 caractères")
        String lieuOuLien,

        @NotEmpty(message = "Au moins un étudiant doit être affecté")
        List<@NotNull Long> etudiantIds

) {}