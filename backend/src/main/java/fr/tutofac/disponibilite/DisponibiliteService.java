package fr.tutofac.disponibilite;

import fr.tutofac.auth.CurrentUser;
import fr.tutofac.config.ConflictException;
import fr.tutofac.config.NotFoundException;
import fr.tutofac.tuteur.ProfilTuteur;
import fr.tutofac.tuteur.ProfilTuteurRepository;
import fr.tutofac.utilisateur.Utilisateur;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class DisponibiliteService {

    private static final Duration PERIODE_HEBDOMADAIRE = Duration.ofDays(7);
    private final DisponibiliteRepository disponibilites;
    private final ProfilTuteurRepository profils;

    public DisponibiliteService(DisponibiliteRepository disponibilites, ProfilTuteurRepository profils) {
        this.disponibilites = disponibilites;
        this.profils = profils;
    }

    @Transactional
    public List<DisponibiliteDto> lister() {
        Utilisateur utilisateur = CurrentUser.get();
        return disponibilites.findByProfilTuteurUtilisateurIdOrderByDebutAsc(utilisateur.getId())
                .stream().map(DisponibiliteDto::from).toList();
    }

    @Transactional
    public DisponibiliteDto creer(DisponibiliteRequest request) {
        ProfilTuteur profil = profilCourantVerrouille();
        valider(request);
        Disponibilite nouvelle = appliquer(new Disponibilite(), profil, request);
        verifierChevauchement(nouvelle, null);
        return DisponibiliteDto.from(disponibilites.save(nouvelle));
    }

    @Transactional
    public DisponibiliteDto modifier(Long id, DisponibiliteRequest request) {
        Disponibilite existante = disponibilites.findById(id)
                .orElseThrow(() -> new NotFoundException("Disponibilité introuvable"));
        verifierProprietaire(existante);
        ProfilTuteur profil = profilVerrouille(existante.getProfilTuteur().getUtilisateur().getId());
        valider(request);
        appliquer(existante, profil, request);
        verifierChevauchement(existante, id);
        return DisponibiliteDto.from(disponibilites.save(existante));
    }

    @Transactional
    public void supprimer(Long id) {
        Disponibilite disponibilite = disponibilites.findById(id)
                .orElseThrow(() -> new NotFoundException("Disponibilité introuvable"));
        verifierProprietaire(disponibilite);
        disponibilites.delete(disponibilite);
    }

    private ProfilTuteur profilCourantVerrouille() {
        return profilVerrouille(CurrentUser.get().getId());
    }

    private ProfilTuteur profilVerrouille(Long utilisateurId) {
        return profils.findByUtilisateurIdForUpdate(utilisateurId)
                .orElseThrow(() -> new NotFoundException("Créez d'abord votre profil tuteur"));
    }

    private void verifierProprietaire(Disponibilite disponibilite) {
        if (!disponibilite.getProfilTuteur().getUtilisateur().getId().equals(CurrentUser.get().getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Vous ne pouvez gérer que vos propres disponibilités");
        }
    }

    private Disponibilite appliquer(Disponibilite disponibilite, ProfilTuteur profil,
                                    DisponibiliteRequest request) {
        disponibilite.setProfilTuteur(profil);
        disponibilite.setDebut(request.debut());
        disponibilite.setFin(request.fin());
        disponibilite.setRecurrence(request.recurrence());
        disponibilite.setFinRecurrence(request.finRecurrence());
        return disponibilite;
    }

    private void valider(DisponibiliteRequest request) {
        if (!request.fin().isAfter(request.debut())) {
            throw new IllegalArgumentException("La fin doit être postérieure au début");
        }
        if (request.recurrence() == Recurrence.HEBDOMADAIRE) {
            if (request.finRecurrence() == null) {
                throw new IllegalArgumentException("La date de fin est obligatoire pour une récurrence hebdomadaire");
            }
            if (request.finRecurrence().isBefore(request.debut().toLocalDate())) {
                throw new IllegalArgumentException("La fin de récurrence doit être après le début du créneau");
            }
            if (Duration.between(request.debut(), request.fin()).compareTo(PERIODE_HEBDOMADAIRE) > 0) {
                throw new IllegalArgumentException("Un créneau hebdomadaire ne peut pas durer plus de 7 jours");
            }
        } else if (request.finRecurrence() != null) {
            throw new IllegalArgumentException("La fin de récurrence ne s'applique qu'aux créneaux hebdomadaires");
        }
    }

    private void verifierChevauchement(Disponibilite candidate, Long idIgnore) {
        List<Disponibilite> existantes = disponibilites.findByProfilTuteurUtilisateurIdOrderByDebutAsc(
                candidate.getProfilTuteur().getUtilisateur().getId());
        boolean chevauche = existantes.stream()
                .filter(d -> !d.getId().equals(idIgnore))
                .anyMatch(d -> seChevauchent(candidate, d));
        if (chevauche) {
            throw new ConflictException("Ce créneau chevauche une autre de vos disponibilités");
        }
    }

    private boolean seChevauchent(Disponibilite a, Disponibilite b) {
        long dernierA = dernierIndice(a);
        long dernierB = dernierIndice(b);
        Duration decalage = Duration.between(a.getDebut(), b.getDebut());
        long centre = decalage.negated().dividedBy(PERIODE_HEBDOMADAIRE);
        long indiceMin = -dernierA;
        long indiceMax = dernierB;
        Duration dureeA = Duration.between(a.getDebut(), a.getFin());
        Duration dureeB = Duration.between(b.getDebut(), b.getFin());

        for (long k = Math.max(indiceMin, centre - 1); k <= Math.min(indiceMax, centre + 1); k++) {
            Duration ecart = decalage.plus(PERIODE_HEBDOMADAIRE.multipliedBy(k));
            if (ecart.compareTo(dureeA) < 0 && ecart.negated().compareTo(dureeB) < 0) {
                return true;
            }
        }
        return false;
    }

    private long dernierIndice(Disponibilite disponibilite) {
        if (disponibilite.getRecurrence() == Recurrence.AUCUNE) return 0;
        long jours = ChronoUnit.DAYS.between(disponibilite.getDebut().toLocalDate(),
                disponibilite.getFinRecurrence());
        return Math.max(0, jours / 7);
    }
}