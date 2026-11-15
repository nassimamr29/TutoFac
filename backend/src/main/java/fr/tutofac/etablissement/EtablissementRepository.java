package fr.tutofac.etablissement;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EtablissementRepository extends JpaRepository<Etablissement, Long> {
    boolean existsByNom(String nom);
}