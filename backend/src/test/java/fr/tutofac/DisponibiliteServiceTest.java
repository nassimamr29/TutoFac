package fr.tutofac;

import fr.tutofac.config.ConflictException;
import fr.tutofac.disponibilite.*;
import fr.tutofac.tuteur.ProfilTuteur;
import fr.tutofac.tuteur.ProfilTuteurRepository;
import fr.tutofac.utilisateur.Role;
import fr.tutofac.utilisateur.Utilisateur;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DisponibiliteServiceTest {

    @Mock
    private DisponibiliteRepository disponibilites;

    @Mock
    private ProfilTuteurRepository profils;

    @InjectMocks
    private DisponibiliteService service;

    private ProfilTuteur profil;

    @BeforeEach
    void authentifierTuteur() {
        Utilisateur utilisateur = new Utilisateur();
        utilisateur.setId(7L);
        utilisateur.setRole(Role.TUTEUR);
        profil = new ProfilTuteur();
        profil.setUtilisateur(utilisateur);
        when(profils.findByUtilisateurIdForUpdate(7L)).thenReturn(Optional.of(profil));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(utilisateur, null,
                        List.of(new SimpleGrantedAuthority("ROLE_TUTEUR"))));
    }

    @AfterEach
    void nettoyerContexte() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void refuseLesCreneauxQuiChevauchentUneRecurrenceHebdomadaire() {
        Disponibilite recurrence = disponibilite(
                LocalDateTime.of(2026, 11, 16, 10, 0),
                LocalDateTime.of(2026, 11, 16, 11, 0),
                Recurrence.HEBDOMADAIRE, LocalDate.of(2026, 12, 14));
        recurrence.setId(2L);
        recurrence.setProfilTuteur(profil);
        when(disponibilites.findByProfilTuteurUtilisateurIdOrderByDebutAsc(7L))
                .thenReturn(List.of(recurrence));

        DisponibiliteRequest request = new DisponibiliteRequest(
                LocalDateTime.of(2026, 11, 23, 10, 30),
                LocalDateTime.of(2026, 11, 23, 11, 30),
                Recurrence.AUCUNE, null);

        assertThrows(ConflictException.class, () -> service.creer(request));
        verify(disponibilites, never()).save(any());
    }

    @Test
    void autoriseDeuxCreneauxHebdomadairesAdjacents() {
        Disponibilite existante = disponibilite(
                LocalDateTime.of(2026, 11, 16, 10, 0),
                LocalDateTime.of(2026, 11, 16, 11, 0),
                Recurrence.HEBDOMADAIRE, LocalDate.of(2026, 12, 14));
        existante.setId(2L);
        existante.setProfilTuteur(profil);
        when(disponibilites.findByProfilTuteurUtilisateurIdOrderByDebutAsc(7L))
                .thenReturn(List.of(existante));
        when(disponibilites.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        DisponibiliteRequest request = new DisponibiliteRequest(
                LocalDateTime.of(2026, 11, 16, 11, 0),
                LocalDateTime.of(2026, 11, 16, 12, 0),
                Recurrence.HEBDOMADAIRE, LocalDate.of(2026, 12, 14));

        assertDoesNotThrow(() -> service.creer(request));
        verify(disponibilites).save(any(Disponibilite.class));
    }

    @Test
    void refuseUneFinEgaleAuDebut() {
        DisponibiliteRequest request = new DisponibiliteRequest(
                LocalDateTime.of(2026, 11, 16, 10, 0),
                LocalDateTime.of(2026, 11, 16, 10, 0),
                Recurrence.AUCUNE, null);

        assertThrows(IllegalArgumentException.class, () -> service.creer(request));
        verify(disponibilites, never()).save(any());
    }

    private Disponibilite disponibilite(LocalDateTime debut, LocalDateTime fin,
                                        Recurrence recurrence, LocalDate finRecurrence) {
        Disponibilite disponibilite = new Disponibilite();
        disponibilite.setDebut(debut);
        disponibilite.setFin(fin);
        disponibilite.setRecurrence(recurrence);
        disponibilite.setFinRecurrence(finRecurrence);
        return disponibilite;
    }
}
