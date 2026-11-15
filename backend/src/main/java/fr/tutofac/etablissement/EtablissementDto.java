package fr.tutofac.etablissement;

public record EtablissementDto(Long id, String nom, TypeEtablissement type,
                               StatutPartenariat statutPartenariat) {
    public static EtablissementDto from(Etablissement e) {
        return new EtablissementDto(e.getId(), e.getNom(), e.getType(), e.getStatutPartenariat());
    }
}