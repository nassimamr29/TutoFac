package fr.tutofac.notification;

import fr.tutofac.notification.EmailTemplates.Email;
import fr.tutofac.seance.Seance;
import fr.tutofac.utilisateur.Utilisateur;
import fr.tutofac.utilisateur.UtilisateurRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    private final JavaMailSender mailSender;
    private final UtilisateurRepository utilisateurRepository;
    private final String expediteur;

    public NotificationService(JavaMailSender mailSender,
                               UtilisateurRepository utilisateurRepository,
                               @Value("${app.mail.from:noreply@tutofac.fr}") String expediteur) {
        this.mailSender = mailSender;
        this.utilisateurRepository = utilisateurRepository;
        this.expediteur = expediteur;
    }

    // Prévient le tuteur et les étudiants d'une nouvelle séance
    public void notifierAffectation(Seance seance, Utilisateur tuteur, Collection<Long> etudiantIds) {
        List<SimpleMailMessage> messages = new ArrayList<>();

        if (tuteur != null) {
            messages.add(message(tuteur, EmailTemplates.affectationTuteur(
                    tuteur.getPrenom(), seance.getDebut(), seance.getFin(), seance.getLieuOuLien())));
        }
        for (Utilisateur etudiant : utilisateurRepository.findAllById(etudiantIds)) {
            messages.add(message(etudiant, EmailTemplates.affectationEtudiant(
                    etudiant.getPrenom(), seance.getDebut(), seance.getFin(), seance.getLieuOuLien())));
        }

        envoyerApresCommit(messages);
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
    private boolean envoyer(SimpleMailMessage message) {
        try {
            mailSender.send(message);
            return true;
        } catch (MailException e) {
            log.warn("Envoi du mail impossible à {} : {}", String.join(",", message.getTo()), e.getMessage());
            return false;
        }
    }

    private SimpleMailMessage message(Utilisateur destinataire, Email email) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(expediteur);
        message.setTo(destinataire.getEmail());
        message.setSubject(email.sujet());
        message.setText(email.corps());
        return message;
    }
}
