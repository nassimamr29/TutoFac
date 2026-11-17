package fr.tutofac.besoin;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BesoinRepository extends JpaRepository<Besoin, Long> {

    // Besoins appartenant à un établissement
    List<Besoin> findByEtablissementId(Long etablissementId);

    // Besoins d'un établissement selon leur statut
    List<Besoin> findByEtablissementIdAndStatut(
        Long etablissementId,
        Besoin.Statut statut
    );
}