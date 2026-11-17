package fr.tutofac.seance;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface SeanceRepository extends JpaRepository<Seance, Long> {

    // Liste les séances d'un besoin
    List<Seance> findByBesoinIdOrderByDebutAsc(Long besoinId);

    // Liste les séances d'un tuteur
    List<Seance> findByProfilTuteurIdOrderByDebutAsc(Long profilTuteurId);

    // Vérifie si un tuteur possède déjà une séance sur ce créneau
    @Query("""
        SELECT COUNT(s) > 0
        FROM Seance s
        WHERE s.profilTuteurId = :profilTuteurId
        AND s.statut <> fr.tutofac.seance.Seance.Statut.ANNULEE
        AND s.debut < :fin
        AND s.fin > :debut
        """)
    boolean existeConflitHoraire(
            @Param("profilTuteurId") Long profilTuteurId,
            @Param("debut") LocalDateTime debut,
            @Param("fin") LocalDateTime fin
    );

    // Liste les séances d'un établissement
    @Query("""
        SELECT s
        FROM Seance s, Besoin b
        WHERE s.besoinId = b.id
        AND b.etablissementId = :etablissementId
        ORDER BY s.debut ASC
        """)
    List<Seance> findByEtablissementId(
            @Param("etablissementId") Long etablissementId
    );
}