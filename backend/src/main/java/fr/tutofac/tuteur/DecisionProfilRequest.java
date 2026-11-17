package fr.tutofac.tuteur;

import jakarta.validation.constraints.Size;

public record DecisionProfilRequest(StatutProfil decision, @Size(max = 500) String motifRefus) {}
