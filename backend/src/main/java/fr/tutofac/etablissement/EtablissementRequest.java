package fr.tutofac.etablissement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record EtablissementRequest(
        @NotBlank String nom,
        @NotNull TypeEtablissement type,
        StatutPartenariat statutPartenariat) {
}