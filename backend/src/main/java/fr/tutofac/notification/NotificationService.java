package fr.tutofac.notification;

import fr.tutofac.notification.EmailTemplates.Email;
import fr.tutofac.seance.Seance;
import fr.tutofac.utilisateur.Utilisateur;
import fr.tutofac.utilisateur.UtilisateurRepository;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/*
 * Envoi des e-mails liés aux séances. Méthodes à appeler depuis le module seance/ :
 *  - notifierAffectation(seance, tuteur, etudiantIds) : à la création d'une séance
 *  - notifierModification(seance) : après un changement d'horaire ou de lieu
 *  - notifierAnnulation(seance)   : après l'annulation
 *  - notifierRappel(seance)       : appelé chaque soir par RappelSeances
 * Dans une transaction, les mails partent seulement après le commit.
 */
@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    // Tuteur de la séance + étudiants inscrits (table participation)
    private static final String SQL_DESTINATAIRES = """
            select pt.utilisateur_id from profil_tuteur pt where pt.id = :profilId
            union
            select p.etudiant_id from participation p where p.seance_id = :seanceId
            """;

    // Un modèle d'e-mail : (prénom, début, fin, lieu) -> Email
    @FunctionalInterface
    interface Modele {
        Email creer(String prenom, LocalDateTime debut, LocalDateTime fin, String lieu);
    }

    private final JavaMailSender mailSender;
    private final UtilisateurRepository utilisateurRepository;
    private final EntityManager entityManager;
    private final String expediteur;

    public NotificationService(JavaMailSender mailSender,
                               UtilisateurRepository utilisateurRepository,
                               EntityManager entityManager,
                               @Value("${app.mail.from:noreply@tutofac.fr}") String expediteur) {
        this.mailSender = mailSender;
        this.utilisateurRepository = utilisateurRepository;
        this.entityManager = entityManager;
        this.expediteur = expediteur;
    }

    // Prévient le tuteur et les étudiants d'une nouvelle séance
    public void notifierAffectation(Seance seance, Utilisateur tuteur, Collection<Long> etudiantIds) {
        List<SimpleMailMessage> messages = new ArrayList<>();

        if (tuteur != null) {
            messages.add(message(tuteur, seance, EmailTemplates::affectationTuteur));
        }
        for (Utilisateur etudiant : utilisateurRepository.findAllById(etudiantIds)) {
            messages.add(message(etudiant, seance, EmailTemplates::affectationEtudiant));
        }

        envoyerApresCommit(messages);
    }

    public void notifierModification(Seance seance) {
        notifierParticipants(seance, EmailTemplates::modification);
    }

    public void notifierAnnulation(Seance seance) {
        notifierParticipants(seance, EmailTemplates::annulation);
    }

    public void notifierRappel(Seance seance) {
        notifierParticipants(seance, EmailTemplates::rappel);
    }

    // Même message pour le tuteur et les étudiants, retrouvés à partir de la séance
    private void notifierParticipants(Seance seance, Modele modele) {
        List<SimpleMailMessage> messages = new ArrayList<>();
        for (Utilisateur destinataire : utilisateurRepository.findAllById(destinataires(seance))) {
            messages.add(message(destinataire, seance, modele));
        }
        envoyerApresCommit(messages);
    }

    List<Long> destinataires(Seance seance) {
        List<?> lignes = entityManager.createNativeQuery(SQL_DESTINATAIRES)
                .setParameter("profilId", seance.getProfilTuteurId())
                .setParameter("seanceId", seance.getId())
                .getResultList();
        return lignes.stream().map(id -> ((Number) id).longValue()).toList();
    }

    // Les mails partent seulement si la transaction est validée (pas de mail pour une séance annulée par un rollback)
    private void envoyerApresCommit(List<SimpleMailMessage> messages) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            messages.forEach(this::envoyer);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                messages.forEach(NotificationService.this::envoyer);
            }
        });
    }

    // Un échec d'envoi est journalisé mais ne fait jamais échouer l'action métier
    private void envoyer(SimpleMailMessage message) {
        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.warn("Envoi du mail impossible à {} : {}", String.join(",", message.getTo()), e.getMessage());
        }
    }

    private SimpleMailMessage message(Utilisateur destinataire, Seance seance, Modele modele) {
        Email email = modele.creer(destinataire.getPrenom(),
                seance.getDebut(), seance.getFin(), seance.getLieuOuLien());
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(expediteur);
        message.setTo(destinataire.getEmail());
        message.setSubject(email.sujet());
        message.setText(email.corps());
        return message;
    }
}
