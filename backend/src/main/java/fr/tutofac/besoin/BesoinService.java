package fr.tutofac.besoin;

import fr.tutofac.auth.CurrentUser;
import fr.tutofac.config.ConflictException;
import fr.tutofac.config.NotFoundException;
import fr.tutofac.utilisateur.Utilisateur;

import jakarta.persistence.EntityManager;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BesoinService {

    private final BesoinRepository repo;
    private final EntityManager entityManager;

    public BesoinService(BesoinRepository repo, EntityManager entityManager) {
        this.repo = repo;
        this.entityManager = entityManager;
    }

    // Liste les besoins de l'établissement connecté
    @Transactional(readOnly = true)
    public List<BesoinDto> lister() {

        Long etablissementId = CurrentUser.etablissementId();

        return repo.findByEtablissementId(etablissementId)
                .stream()
                .map(BesoinDto::from)
                .toList();
    }

    // Consulte un besoin
    @Transactional(readOnly = true)
    public BesoinDto trouver(Long id) {
        return BesoinDto.from(chercher(id));
    }

    // Crée un besoin de tutorat
    @Transactional
    public BesoinDto creer(BesoinRequest r) {

        Utilisateur utilisateur = CurrentUser.get();
        Long etablissementId = CurrentUser.etablissementId();

        if (etablissementId == null) {
            throw new IllegalArgumentException(
                    "Un établissement est nécessaire pour créer un besoin"
            );
        }

        // Vérification des dates
        if (r.periodeFin().isBefore(r.periodeDebut())) {
            throw new IllegalArgumentException(
                    "La date de fin doit être supérieure ou égale à la date de début"
            );
        }

        // Vérification de l'effectif
        if (r.effectif() <= 0) {
            throw new IllegalArgumentException(
                    "L'effectif doit être supérieur à zéro"
            );
        }

        // Vérification de l'existence de la matière
        Number count = (Number) entityManager
                .createNativeQuery(
                        "SELECT COUNT(*) FROM matiere WHERE id = :id"
                )
                .setParameter("id", r.matiereId())
                .getSingleResult();

        if (count.longValue() == 0) {
            throw new NotFoundException("Matière introuvable");
        }

        // Création du besoin
        Besoin besoin = new Besoin();

        besoin.setEtablissementId(etablissementId);
        besoin.setMatiereId(r.matiereId());
        besoin.setCreePar(utilisateur.getId());
        besoin.setNiveau(r.niveau());
        besoin.setEffectif(r.effectif());
        besoin.setPeriodeDebut(r.periodeDebut());
        besoin.setPeriodeFin(r.periodeFin());
        besoin.setStatut(Besoin.Statut.OUVERT);

        return BesoinDto.from(repo.save(besoin));
    }

    // Annule un besoin
    @Transactional
    public BesoinDto annuler(Long id) {

        Besoin besoin = chercher(id);

        if (besoin.getStatut() == Besoin.Statut.ANNULE) {
            throw new ConflictException("Ce besoin est déjà annulé");
        }

        besoin.setStatut(Besoin.Statut.ANNULE);

        return BesoinDto.from(repo.save(besoin));
    }

    // Recherche un besoin et vérifie les droits d'accès
    private Besoin chercher(Long id) {

        Besoin besoin = repo.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Besoin introuvable")
                );

        CurrentUser.checkEtablissement(besoin.getEtablissementId());

        return besoin;
    }
}