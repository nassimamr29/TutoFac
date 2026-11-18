package fr.tutofac;

import fr.tutofac.auth.AuthService;
import fr.tutofac.auth.JwtService;
import fr.tutofac.auth.LoginRequest;
import fr.tutofac.auth.LoginResponse;
import fr.tutofac.etablissement.Etablissement;
import fr.tutofac.utilisateur.Role;
import fr.tutofac.utilisateur.Utilisateur;
import fr.tutofac.utilisateur.UtilisateurRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UtilisateurRepository utilisateurs;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService service;

    @Test
    void loginRenvoieLeTokenLeRoleEtLEtablissement() {
        Utilisateur gestionnaire = utilisateur(Role.GESTIONNAIRE, true);
        Etablissement etab = new Etablissement();
        etab.setId(1L);
        etab.setNom("Université Sorbonne Paris Nord");
        gestionnaire.setEtablissement(etab);
        when(utilisateurs.findByEmail("gestionnaire.spn@tutofac.fr")).thenReturn(Optional.of(gestionnaire));
        when(encoder.matches("Demo1234!", "hash")).thenReturn(true);
        when(jwtService.generateToken(gestionnaire)).thenReturn("jwt");

        // l'email saisi est normalisé (espaces et majuscules)
        LoginResponse r = service.login(new LoginRequest("  Gestionnaire.SPN@tutofac.fr ", "Demo1234!"));

        assertEquals("jwt", r.token());
        assertEquals(Role.GESTIONNAIRE, r.role());
        assertEquals(1L, r.etablissementId());
        assertEquals("Université Sorbonne Paris Nord", r.etablissementNom());
    }

    @Test
    void unAdminSansEtablissementPeutSeConnecter() {
        Utilisateur admin = utilisateur(Role.ADMIN, true);
        when(utilisateurs.findByEmail("admin@tutofac.fr")).thenReturn(Optional.of(admin));
        when(encoder.matches("Demo1234!", "hash")).thenReturn(true);
        when(jwtService.generateToken(admin)).thenReturn("jwt");

        LoginResponse r = service.login(new LoginRequest("admin@tutofac.fr", "Demo1234!"));

        assertEquals(Role.ADMIN, r.role());
        assertNull(r.etablissementId());
        assertNull(r.etablissementNom());
    }

    @Test
    void mauvaisMotDePasseRefuseSansToken() {
        when(utilisateurs.findByEmail("admin@tutofac.fr")).thenReturn(Optional.of(utilisateur(Role.ADMIN, true)));
        when(encoder.matches("faux", "hash")).thenReturn(false);

        BadCredentialsException e = assertThrows(BadCredentialsException.class,
                () -> service.login(new LoginRequest("admin@tutofac.fr", "faux")));

        assertEquals("Email ou mot de passe incorrect", e.getMessage());
        verifyNoInteractions(jwtService);
    }

    @Test
    void emailInconnuDonneLeMemeMessageQuUnMauvaisMotDePasse() {
        when(utilisateurs.findByEmail("inconnu@tutofac.fr")).thenReturn(Optional.empty());

        BadCredentialsException e = assertThrows(BadCredentialsException.class,
                () -> service.login(new LoginRequest("inconnu@tutofac.fr", "Demo1234!")));

        assertEquals("Email ou mot de passe incorrect", e.getMessage());
        verifyNoInteractions(encoder, jwtService);
    }

    @Test
    void compteDesactiveRefuseMemeAvecLeBonMotDePasse() {
        when(utilisateurs.findByEmail("etudiant1@tutofac.fr")).thenReturn(Optional.of(utilisateur(Role.ETUDIANT, false)));

        assertThrows(BadCredentialsException.class,
                () -> service.login(new LoginRequest("etudiant1@tutofac.fr", "Demo1234!")));

        verify(encoder, never()).matches(anyString(), anyString());
        verify(jwtService, never()).generateToken(any());
    }

    private static Utilisateur utilisateur(Role role, boolean actif) {
        Utilisateur u = new Utilisateur();
        u.setId(5L);
        u.setEmail("compte@tutofac.fr");
        u.setMotDePasse("hash");
        u.setRole(role);
        u.setActif(actif);
        return u;
    }
}
