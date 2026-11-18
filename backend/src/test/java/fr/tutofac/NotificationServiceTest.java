package fr.tutofac;

import fr.tutofac.notification.EmailTemplates;
import fr.tutofac.notification.NotificationService;
import fr.tutofac.seance.Seance;
import fr.tutofac.utilisateur.Role;
import fr.tutofac.utilisateur.Utilisateur;
import fr.tutofac.utilisateur.UtilisateurRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final LocalDateTime DEBUT = LocalDateTime.of(2026, 12, 3, 14, 0);
    private static final LocalDateTime FIN = LocalDateTime.of(2026, 12, 3, 16, 0);

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UtilisateurRepository utilisateurs;

    private NotificationService service;
    private Seance seance;
    private Utilisateur tuteur;

    @BeforeEach
    void preparer() {
        service = new NotificationService(mailSender, utilisateurs, "noreply@tutofac.fr");
        seance = new Seance();
        seance.setDebut(DEBUT);
        seance.setFin(FIN);
        seance.setLieuOuLien("Salle B204");
        tuteur = utilisateur(10L, "Lina", "tuteur1@tutofac.fr", Role.TUTEUR);
    }

    @AfterEach
    void nettoyer() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void affectationEnvoieUnMailAuTuteurEtAChaqueEtudiant() {
        when(utilisateurs.findAllById(Set.of(20L, 21L))).thenReturn(List.of(
                utilisateur(20L, "Hugo", "etudiant1@tutofac.fr", Role.ETUDIANT),
                utilisateur(21L, "Sara", "etudiant2@tutofac.fr", Role.ETUDIANT)));

        service.notifierAffectation(seance, tuteur, Set.of(20L, 21L));

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(3)).send(captor.capture());
        List<String> destinataires = captor.getAllValues().stream().map(m -> m.getTo()[0]).toList();
        assertEquals(List.of("tuteur1@tutofac.fr", "etudiant1@tutofac.fr", "etudiant2@tutofac.fr"), destinataires);
        assertTrue(captor.getAllValues().get(0).getText().contains("Bonjour Lina"));
        assertEquals("noreply@tutofac.fr", captor.getAllValues().get(0).getFrom());
    }

    @Test
    void unEchecSmtpNeBloquePasLesAutresEnvoisNiLAppelant() {
        when(utilisateurs.findAllById(Set.of(20L))).thenReturn(List.of(
                utilisateur(20L, "Hugo", "etudiant1@tutofac.fr", Role.ETUDIANT)));
        doThrow(new MailSendException("SMTP indisponible"))
                .doNothing()
                .when(mailSender).send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() -> service.notifierAffectation(seance, tuteur, Set.of(20L)));
        verify(mailSender, times(2)).send(any(SimpleMailMessage.class));
    }

    @Test
    void dansUneTransactionLesMailsPartentSeulementApresLeCommit() {
        when(utilisateurs.findAllById(Set.of())).thenReturn(List.of());
        TransactionSynchronizationManager.initSynchronization();

        service.notifierAffectation(seance, tuteur, Set.of());
        verifyNoInteractions(mailSender);

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);
        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void pasDeMailSiLaTransactionEstAnnulee() {
        when(utilisateurs.findAllById(Set.of())).thenReturn(List.of());
        TransactionSynchronizationManager.initSynchronization();

        service.notifierAffectation(seance, tuteur, Set.of());
        // rollback : afterCommit n'est jamais appelé
        TransactionSynchronizationManager.getSynchronizations()
                .forEach(s -> s.afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK));

        verifyNoInteractions(mailSender);
    }

    @Test
    void leTemplateContientLaDateLHoraireEtLeLieu() {
        EmailTemplates.Email email = EmailTemplates.affectationEtudiant("Hugo", DEBUT, FIN, "Salle B204");

        assertTrue(email.sujet().contains("03/12/2026"));
        assertTrue(email.corps().contains("Horaire : 14:00 - 16:00"));
        assertTrue(email.corps().contains("Lieu ou lien : Salle B204"));
    }

    @Test
    void unLieuVideEstAfficheAPreciser() {
        EmailTemplates.Email email = EmailTemplates.affectationTuteur("Lina", DEBUT, FIN, "  ");

        assertTrue(email.corps().contains("Lieu ou lien : à préciser"));
    }

    private static Utilisateur utilisateur(Long id, String prenom, String email, Role role) {
        Utilisateur u = new Utilisateur();
        u.setId(id);
        u.setPrenom(prenom);
        u.setEmail(email);
        u.setRole(role);
        return u;
    }
}
