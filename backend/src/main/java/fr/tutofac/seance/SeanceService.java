package fr.tutofac.seance;

import fr.tutofac.auth.CurrentUser;
import fr.tutofac.besoin.Besoin;
import fr.tutofac.besoin.BesoinRepository;
import fr.tutofac.besoin.RechercheTuteurService;
import fr.tutofac.config.ConflictException;
import fr.tutofac.config.NotFoundException;
import fr.tutofac.notification.NotificationService;
import fr.tutofac.tuteur.ProfilTuteur;
import fr.tutofac.utilisateur.Role;
import fr.tutofac.utilisateur.Utilisateur;
import fr.tutofac.utilisateur.UtilisateurRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.HashSet;

@Service
public class SeanceService {

    private final SeanceRepository seanceRepository;
    private final BesoinRepository besoinRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final RechercheTuteurService rechercheService;
    private final EntityManager entityManager;
    private final NotificationService notificationService;

    public SeanceService(
            SeanceRepository seanceRepository,
            BesoinRepository besoinRepository,
            UtilisateurRepository utilisateurRepository,
            RechercheTuteurService rechercheService,
            EntityManager entityManager,
            NotificationService notificationService) {

        this.seanceRepository = seanceRepository;
        this.besoinRepository = besoinRepository;
        this.utilisateurRepository = utilisateurRepository;
        this.rechercheService = rechercheService;
        this.entityManager = entityManager;
        this.notificationService = notificationService;
    }

    // Liste les séances de l'établissement connecté
    @Transactional(readOnly = true)
    public List<SeanceDto> lister() {

        Long etablissementId = CurrentUser.etablissementId();

        if (etablissementId == null) {
            throw new IllegalArgumentException(
                    "Un établissement est nécessaire"
            );
        }

        return seanceRepository.findByEtablissementId(etablissementId)
                .stream()
                .map(this::versDto)
                .toList();
    }

    // Consulte une séance
    @Transactional(readOnly = true)
    public SeanceDto trouver(Long id) {
        return versDto(chercher(id));
    }

    // Liste les séances associées à un besoin
    @Transactional(readOnly = true)
    public List<SeanceDto> listerParBesoin(Long besoinId) {

        Besoin besoin = chercherBesoin(besoinId);

        return seanceRepository
                .findByBesoinIdOrderByDebutAsc(besoin.getId())
                .stream()
                .map(this::versDto)
                .toList();
    }

    // Crée une séance et affecte un tuteur et des étudiants
    @Transactional
    public SeanceDto creer(SeanceRequest request) {

        if (request.debut() == null ||
                request.fin() == null ||
                !request.fin().isAfter(request.debut())) {

            throw new IllegalArgumentException(
                    "La date de fin doit être après la date de début"
            );
        }

        Besoin besoin = chercherBesoin(request.besoinId());

        if (besoin.getStatut() == Besoin.Statut.ANNULE) {
            throw new ConflictException(
                    "Impossible de créer une séance pour un besoin annulé"
            );
        }

        // Vérification de la période du besoin
        if (request.debut().toLocalDate()
                .isBefore(besoin.getPeriodeDebut()) ||
                request.fin().minusNanos(1).toLocalDate()
                .isAfter(besoin.getPeriodeFin())) {

            throw new IllegalArgumentException(
                    "La séance doit être comprise dans la période du besoin"
            );
        }

        // Vérification de la liste des étudiants
        if (request.etudiantIds() == null ||
                request.etudiantIds().isEmpty()) {

            throw new IllegalArgumentException(
                    "Au moins un étudiant est obligatoire"
            );
        }

        if (request.etudiantIds().stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException(
                    "Identifiant étudiant invalide"
            );
        }

        Set<Long> idsUniques = new HashSet<>(request.etudiantIds());

        if (idsUniques.size() != request.etudiantIds().size()) {
            throw new IllegalArgumentException(
                    "Un étudiant ne peut pas être ajouté deux fois"
            );
        }

        if (idsUniques.size() > besoin.getEffectif()) {
            throw new IllegalArgumentException(
                    "Le nombre d'étudiants dépasse l'effectif du besoin"
            );
        }

        // Vérification des étudiants
        for (Long etudiantId : idsUniques) {

            Utilisateur etudiant = utilisateurRepository
                    .findById(etudiantId)
                    .orElseThrow(() ->
                            new NotFoundException(
                                    "Étudiant introuvable : " + etudiantId
                            )
                    );

            if (etudiant.getRole() != Role.ETUDIANT) {
                throw new IllegalArgumentException(
                        "L'utilisateur " + etudiantId
                                + " n'est pas un étudiant"
                );
            }

            if (!etudiant.isActif()) {
                throw new ConflictException(
                        "L'étudiant " + etudiantId + " est inactif"
                );
            }

            if (etudiant.getEtablissement() == null ||
                    !Objects.equals(
                            etudiant.getEtablissement().getId(),
                            besoin.getEtablissementId())) {

                throw new IllegalArgumentException(
                        "L'étudiant " + etudiantId
                                + " n'appartient pas à cet établissement"
                );
            }
        }

        if (request.profilTuteurId() == null) {
            throw new IllegalArgumentException(
                    "Le tuteur est obligatoire"
            );
        }

        // Verrouille le profil pour éviter deux affectations simultanées
        ProfilTuteur profil = entityManager.find(
                ProfilTuteur.class,
                request.profilTuteurId(),
                LockModeType.PESSIMISTIC_WRITE
        );

        if (profil == null) {
            throw new NotFoundException("Tuteur introuvable");
        }

        // Vérification de la qualification et de la disponibilité
        boolean tuteurDisponible = rechercheService
                .rechercher(
                        besoin.getId(),
                        request.debut(),
                        request.fin()
                )
                .stream()
                .anyMatch(t ->
                        t.profilTuteurId().equals(profil.getId())
                );

        if (!tuteurDisponible) {

            // Distingue un conflit horaire d'une indisponibilité
            if (seanceRepository.existeConflitHoraire(
                    profil.getId(),
                    request.debut(),
                    request.fin())) {

                throw new ConflictException(
                        "Le tuteur possède déjà une séance sur ce créneau"
                );
            }

            throw new ConflictException(
                    "Le tuteur n'est pas qualifié ou disponible sur ce créneau"
            );
        }

        // Création de la séance
        Seance seance = new Seance();

        seance.setBesoinId(besoin.getId());
        seance.setProfilTuteurId(profil.getId());
        seance.setDebut(request.debut());
        seance.setFin(request.fin());
        seance.setLieuOuLien(request.lieuOuLien());
        seance.setStatut(Seance.Statut.PLANIFIEE);

        seance = seanceRepository.saveAndFlush(seance);

        // Enregistrement des participations
        for (Long etudiantId : idsUniques) {

            entityManager.createNativeQuery(
                    """
                    INSERT INTO participation (seance_id, etudiant_id)
                    VALUES (:seanceId, :etudiantId)
                    """
            )
            .setParameter("seanceId", seance.getId())
            .setParameter("etudiantId", etudiantId)
            .executeUpdate();
        }

        // Partie 4 : mail au tuteur et aux étudiants (envoyé après le commit)
        notificationService.notifierAffectation(seance, profil.getUtilisateur(), idsUniques);

        return versDto(seance);
    }

    // Annule une séance
    @Transactional
    public SeanceDto annuler(Long id) {

        Seance seance = chercher(id);

        if (seance.getStatut() == Seance.Statut.ANNULEE) {
            throw new ConflictException(
                    "Cette séance est déjà annulée"
            );
        }

        if (seance.getStatut() == Seance.Statut.REALISEE) {
            throw new ConflictException(
                    "Une séance réalisée ne peut pas être annulée"
            );
        }

        seance.setStatut(Seance.Statut.ANNULEE);

        return versDto(seanceRepository.save(seance));
    }

    // Marque une séance comme réalisée
    @Transactional
    public SeanceDto realiser(Long id) {

        Seance seance = chercher(id);

        if (seance.getStatut() != Seance.Statut.PLANIFIEE) {
            throw new ConflictException(
                    "Seule une séance planifiée peut être réalisée"
            );
        }

        if (seance.getFin().isAfter(LocalDateTime.now())) {
            throw new ConflictException(
                    "La séance n'est pas encore terminée"
            );
        }

        seance.setStatut(Seance.Statut.REALISEE);

        return versDto(seanceRepository.save(seance));
    }

    // Recherche un besoin et contrôle l'établissement
    private Besoin chercherBesoin(Long id) {

        Besoin besoin = besoinRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Besoin introuvable")
                );

        CurrentUser.checkEtablissement(besoin.getEtablissementId());

        return besoin;
    }

    // Recherche une séance et contrôle les droits
    private Seance chercher(Long id) {

        Seance seance = seanceRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("Séance introuvable")
                );

        chercherBesoin(seance.getBesoinId());

        return seance;
    }

    // Transforme une séance en DTO
    private SeanceDto versDto(Seance seance) {

        List<?> resultats = entityManager.createNativeQuery(
                """
                SELECT etudiant_id
                FROM participation
                WHERE seance_id = :seanceId
                ORDER BY etudiant_id
                """
        )
        .setParameter("seanceId", seance.getId())
        .getResultList();

        List<Long> etudiantIds = resultats.stream()
                .map(value -> ((Number) value).longValue())
                .toList();

        return new SeanceDto(
                seance.getId(),
                seance.getBesoinId(),
                seance.getProfilTuteurId(),
                seance.getDebut(),
                seance.getFin(),
                seance.getLieuOuLien(),
                seance.getStatut(),
                seance.getCreatedAt(),
                etudiantIds
        );
    }
}