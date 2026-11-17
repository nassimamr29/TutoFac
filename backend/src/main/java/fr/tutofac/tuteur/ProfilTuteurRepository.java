package fr.tutofac.tuteur;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface ProfilTuteurRepository extends JpaRepository<ProfilTuteur, Long> {
    Optional<ProfilTuteur> findByUtilisateurId(Long utilisateurId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProfilTuteur p where p.utilisateur.id = :utilisateurId")
    Optional<ProfilTuteur> findByUtilisateurIdForUpdate(@Param("utilisateurId") Long utilisateurId);
    List<ProfilTuteur> findByUtilisateurEtablissementIdAndStatutOrderByCreatedAtAsc(
            Long etablissementId, StatutProfil statut);
    @Query("select p from ProfilTuteur p where p.statut = fr.tutofac.tuteur.StatutProfil.VALIDE")
    List<ProfilTuteur> findTuteursProposables();

    @Query("""
            select distinct p from ProfilTuteur p join p.matieres m
            where p.statut = fr.tutofac.tuteur.StatutProfil.VALIDE and m.id = :matiereId
            """)
    List<ProfilTuteur> findTuteursProposablesParMatiere(@Param("matiereId") Long matiereId);
}