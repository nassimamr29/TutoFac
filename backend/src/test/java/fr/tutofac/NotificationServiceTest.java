package fr.tutofac;

import fr.tutofac.notification.EmailTemplates;
import fr.tutofac.notification.NotificationService;
import fr.tutofac.seance.Seance;
import fr.tutofac.utilisateur.Role;
import fr.tutofac.utilisateur.Utilisateur;
import fr.tutofac.utilisateur.UtilisateurRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    private static final LocalDateTime DEBUT = LocalDateTime.of(2026, 12, 3, 14, 0);
    private static final LocalDateTime FIN = LocalDateTime.of(2026, 12, 3, 16, 0);

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UtilisateurRepository utilisateurs;

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query requete;

    private NotificationService service;
    private Seance seance;
    private Utilisateur tuteur;

    @BeforeEach
    void preparer() {
        service = new NotificationService(mailSender, utilisateurs, entityManager, "noreply@tutofac.fr");
        seance = new Seance();
        seance.setId(5L);
        seance.setProfilTuteurId(3L);
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

    // --- Modification, annulation, rappel : destinataires retrouvés depuis la séance ---

    @Test
    void annulationEnvoieUnMailAuTuteurEtAuxEtudiantsInscrits() {
        destinatairesEnBase(10L, 20L);
        when(utilisateurs.findAllById(List.of(10L, 20L))).thenReturn(List.of(
                tuteur, utilisateur(20L, "Hugo", "etudiant1@tutofac.fr", Role.ETUDIANT)));

        service.notifierAnnulation(seance);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(captor.capture());
        assertEquals(List.of("tuteur1@tutofac.fr", "etudiant1@tutofac.fr"),
                captor.getAllValues().stream().map(m -> m.getTo()[0]).toList());
        assertTrue(captor.getValue().getSubject().contains("annulation"));
        verify(requete).setParameter("profilId", 3L);
        verify(requete).setParameter("seanceId", 5L);
    }

    @Test
    void modificationEnvoieLesNouvellesInformations() {
        destinatairesEnBase(10L);
        when(utilisateurs.findAllById(List.of(10L))).thenReturn(List.of(tuteur));
        seance.setLieuOuLien("Salle C101");

        service.notifierModification(seance);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());
        assertTrue(captor.getValue().getText().contains("Lieu ou lien : Salle C101"));
    }

    @Test
    void rappelSansParticipantNEnvoieRien() {
        destinatairesEnBase();
        when(utilisateurs.findAllById(List.of())).thenReturn(List.of());

        service.notifierRappel(seance);

        verifyNoInteractions(mailSender);
    }

    @Test
    void lesQuatreTemplatesOntUnSujetDistinct() {
        Set<String> sujets = Set.of(
                EmailTemplates.affectationTuteur("Lina", DEBUT, FIN, null).sujet(),
                EmailTemplates.rappel("Lina", DEBUT, FIN, null).sujet(),
                EmailTemplates.modification("Lina", DEBUT, FIN, null).sujet(),
                EmailTemplates.annulation("Lina", DEBUT, FIN, null).sujet());

        assertEquals(4, sujets.size());
        assertTrue(EmailTemplates.rappel("Lina", DEBUT, FIN, null).sujet().contains("rappel"));
    }

    // La requête SQL des destinataires renvoie des BIGINT (Long)
    private void destinatairesEnBase(Long... ids) {
        when(entityManager.createNativeQuery(anyString())).thenReturn(requete);
        when(requete.setParameter(anyString(), any())).thenReturn(requete);
        when(requete.getResultList()).thenReturn(List.of((Object[]) ids));
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
