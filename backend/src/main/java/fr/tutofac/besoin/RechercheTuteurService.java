package fr.tutofac.besoin;

import fr.tutofac.auth.CurrentUser;
import fr.tutofac.config.NotFoundException;
import fr.tutofac.disponibilite.Disponibilite;
import fr.tutofac.disponibilite.Recurrence;
import fr.tutofac.tuteur.ProfilTuteur;
import fr.tutofac.tuteur.ProfilTuteurRepository;
import fr.tutofac.utilisateur.Utilisateur;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class RechercheTuteurService {

    private final BesoinRepository besoinRepository;
    private final ProfilTuteurRepository tuteurRepository;
    private final EntityManager entityManager;

    public RechercheTuteurService(
            BesoinRepository besoinRepository,
            ProfilTuteurRepository tuteurRepository,
            EntityManager entityManager) {

        this.besoinRepository = besoinRepository;
        this.tuteurRepository = tuteurRepository;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<TuteurRechercheDto> rechercher(
            Long besoinId,
            LocalDateTime debut,
            LocalDateTime fin) {

        // Vérification du créneau
        if (debut == null || fin == null || !fin.isAfter(debut)) {
            throw new IllegalArgumentException("Créneau horaire invalide");
        }

        // Recherche du besoin
        Besoin besoin = besoinRepository.findById(besoinId)
                .orElseThrow(() ->
                        new NotFoundException("Besoin introuvable")
                );

        // Le gestionnaire ne peut consulter que ses besoins
        CurrentUser.checkEtablissement(besoin.getEtablissementId());

        if (besoin.getStatut() == Besoin.Statut.ANNULE) {
            throw new IllegalArgumentException("Ce besoin est annulé");
        }

        // Le créneau doit appartenir à la période du besoin
        if (debut.toLocalDate().isBefore(besoin.getPeriodeDebut())
                || fin.minusNanos(1).toLocalDate()
                        .isAfter(besoin.getPeriodeFin())) {

            throw new IllegalArgumentException(
                    "Le créneau doit être compris dans la période du besoin"
            );
        }

        // Recherche sur tous les établissements partenaires
        List<ProfilTuteur> candidats =
                tuteurRepository.findTuteursProposablesParMatiere(
                        besoin.getMatiereId()
                );

        List<TuteurRechercheDto> resultats = new ArrayList<>();

        for (ProfilTuteur profil : candidats) {

            Utilisateur utilisateur = profil.getUtilisateur();

            // Le compte doit être actif
            if (!utilisateur.isActif()) {
                continue;
            }

            // Le tuteur doit avoir un niveau suffisant
            if (profil.getNiveauEtudes().ordinal()
                    < besoin.getNiveau().ordinal()) {
                continue;
            }

            // Vérification des disponibilités
            if (!estDisponible(profil.getId(), debut, fin)) {
                continue;
            }

            // Exclusion des tuteurs déjà affectés
            if (aUnConflit(profil.getId(), debut, fin)) {
                continue;
            }

            resultats.add(new TuteurRechercheDto(
                    profil.getId(),
                    utilisateur.getId(),
                    utilisateur.getNom(),
                    utilisateur.getPrenom(),
                    profil.getFormation(),
                    profil.getNiveauEtudes().name(),
                    utilisateur.getEtablissement().getId(),
                    utilisateur.getEtablissement().getNom()
            ));
        }

        return resultats;
    }

    // Vérifie si une disponibilité couvre entièrement le créneau
    private boolean estDisponible(
            Long profilId,
            LocalDateTime debut,
            LocalDateTime fin) {

        List<Disponibilite> disponibilites = entityManager
                .createQuery(
                        """
                        SELECT d FROM Disponibilite d
                        WHERE d.profilTuteur.id = :profilId
                        """,
                        Disponibilite.class
                )
                .setParameter("profilId", profilId)
                .getResultList();

        for (Disponibilite dispo : disponibilites) {

            // Disponibilité ponctuelle
            if (dispo.getRecurrence() == Recurrence.AUCUNE) {

                if (!debut.isBefore(dispo.getDebut())
                        && !fin.isAfter(dispo.getFin())) {
                    return true;
                }

                continue;
            }

            // Disponibilité hebdomadaire
            if (dispo.getRecurrence() == Recurrence.HEBDOMADAIRE) {

                long semaines = ChronoUnit.WEEKS.between(
                        dispo.getDebut().toLocalDate(),
                        debut.toLocalDate()
                );

                // Vérifie les occurrences proches du créneau demandé
                for (long i = Math.max(0, semaines - 1);
                        i <= Math.max(0, semaines + 1); i++) {

                    LocalDateTime occurrenceDebut =
                            dispo.getDebut().plusWeeks(i);

                    LocalDateTime occurrenceFin =
                            dispo.getFin().plusWeeks(i);

                    // Vérification de la fin de récurrence
                    if (dispo.getFinRecurrence() != null
                            && occurrenceDebut.toLocalDate()
                                    .isAfter(dispo.getFinRecurrence())) {
                        continue;
                    }

                    if (!debut.isBefore(occurrenceDebut)
                            && !fin.isAfter(occurrenceFin)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // Détecte un chevauchement avec une séance existante
    private boolean aUnConflit(
            Long profilId,
            LocalDateTime debut,
            LocalDateTime fin) {

        Number count = (Number) entityManager
                .createNativeQuery(
                        """
                        SELECT COUNT(*)
                        FROM seance
                        WHERE profil_tuteur_id = :profilId
                        AND statut <> 'ANNULEE'
                        AND debut < :fin
                        AND fin > :debut
                        """
                )
                .setParameter("profilId", profilId)
                .setParameter("debut", debut)
                .setParameter("fin", fin)
                .getSingleResult();

        return count.longValue() > 0;
    }
}