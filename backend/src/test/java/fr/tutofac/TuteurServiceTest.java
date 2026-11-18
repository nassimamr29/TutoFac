package fr.tutofac;

import fr.tutofac.config.ConflictException;
import fr.tutofac.config.NotFoundException;
import fr.tutofac.etablissement.Etablissement;
import fr.tutofac.matiere.Matiere;
import fr.tutofac.matiere.MatiereRepository;
import fr.tutofac.matiere.NiveauEtudes;
import fr.tutofac.tuteur.*;
import fr.tutofac.utilisateur.Role;
import fr.tutofac.utilisateur.Utilisateur;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TuteurServiceTest {

    @Mock
    private ProfilTuteurRepository profils;

    @Mock
    private MatiereRepository matieres;

    @TempDir
    Path relevesDir;

    private TuteurService service;
    private Etablissement spn;
    private Utilisateur tuteur;

    @BeforeEach
    void preparer() {
        service = new TuteurService(profils, matieres, relevesDir);
        spn = etablissement(1L);
        tuteur = utilisateur(7L, Role.TUTEUR, spn);
    }

    @AfterEach
    void nettoyerContexte() {
        SecurityContextHolder.clearContext();
    }

    // --- Profil du tuteur ---

    @Test
    void unPremierProfilEstCreeEnAttenteAvecLesMatieresChoisies() {
        connecter(tuteur);
        Matiere algo = mock(Matiere.class);
        when(matieres.findAllById(Set.of(1L))).thenReturn(List.of(algo));
        when(profils.findByUtilisateurId(7L)).thenReturn(Optional.empty());
        when(profils.save(any())).thenAnswer(i -> i.getArgument(0));

        ProfilTuteurDto dto = service.enregistrerProfil(
                new ProfilTuteurRequest("  Master Informatique ", NiveauEtudes.M1, List.of(1L)));

        assertEquals("Master Informatique", dto.formation());
        assertEquals(StatutProfil.EN_ATTENTE, dto.statut());
        assertEquals(7L, dto.utilisateurId());
        assertEquals(1, dto.matieres().size());
    }

    @Test
    void modifierUnProfilValideLeRemetEnAttente() {
        connecter(tuteur);
        ProfilTuteur existant = profil(tuteur, StatutProfil.REFUSE);
        existant.setMotifRefus("Relevé illisible");
        when(matieres.findAllById(Set.of(1L))).thenReturn(List.of(mock(Matiere.class)));
        when(profils.findByUtilisateurId(7L)).thenReturn(Optional.of(existant));
        when(profils.save(any())).thenAnswer(i -> i.getArgument(0));

        ProfilTuteurDto dto = service.enregistrerProfil(
                new ProfilTuteurRequest("Master Informatique", NiveauEtudes.M1, List.of(1L)));

        assertEquals(StatutProfil.EN_ATTENTE, dto.statut());
        assertNull(dto.motifRefus());
    }

    @Test
    void desMatieresEnDoublonSontRefusees() {
        connecter(tuteur);

        assertThrows(IllegalArgumentException.class, () -> service.enregistrerProfil(
                new ProfilTuteurRequest("Master", NiveauEtudes.M1, List.of(1L, 1L))));
        verify(profils, never()).save(any());
    }

    @Test
    void uneMatiereInexistanteEstRefusee() {
        connecter(tuteur);
        when(matieres.findAllById(Set.of(1L, 99L))).thenReturn(List.of(mock(Matiere.class)));

        assertThrows(IllegalArgumentException.class, () -> service.enregistrerProfil(
                new ProfilTuteurRequest("Master", NiveauEtudes.M1, List.of(1L, 99L))));
    }

    @Test
    void seulUnTuteurPeutEnregistrerUnProfil() {
        connecter(utilisateur(8L, Role.ETUDIANT, spn));

        assertThrows(AccessDeniedException.class, () -> service.enregistrerProfil(
                new ProfilTuteurRequest("Master", NiveauEtudes.M1, List.of(1L))));
    }

    // --- Relevé de notes ---

    @Test
    void unPdfValideEstStockeEtRemetLeProfilEnAttente() throws Exception {
        connecter(tuteur);
        ProfilTuteur profil = profil(tuteur, StatutProfil.VALIDE);
        when(profils.findByUtilisateurId(7L)).thenReturn(Optional.of(profil));
        when(profils.save(any())).thenAnswer(i -> i.getArgument(0));

        ProfilTuteurDto dto = service.deposerReleve(new MockMultipartFile(
                "fichier", "releve.pdf", "application/pdf", "%PDF-1.4 contenu".getBytes()));

        assertTrue(dto.relevePresent());
        assertEquals(StatutProfil.EN_ATTENTE, dto.statut());
        assertTrue(Files.exists(relevesDir.resolve(profil.getRelevePath())));
    }

    @Test
    void unFichierQuiNEstPasUnVraiPdfEstRefuse() {
        connecter(tuteur);
        when(profils.findByUtilisateurId(7L)).thenReturn(Optional.of(profil(tuteur, StatutProfil.EN_ATTENTE)));

        // type annoncé PDF mais contenu différent
        assertThrows(IllegalArgumentException.class, () -> service.deposerReleve(new MockMultipartFile(
                "fichier", "releve.pdf", "application/pdf", "pas un pdf".getBytes())));
        verify(profils, never()).save(any());
    }

    @Test
    void unFichierNonPdfEstRefuse() {
        connecter(tuteur);
        when(profils.findByUtilisateurId(7L)).thenReturn(Optional.of(profil(tuteur, StatutProfil.EN_ATTENTE)));

        assertThrows(IllegalArgumentException.class, () -> service.deposerReleve(new MockMultipartFile(
                "fichier", "releve.png", "image/png", new byte[]{1, 2, 3})));
    }

    // --- Décision du gestionnaire ---

    @Test
    void leGestionnaireValideUnProfilAvecReleve() {
        Utilisateur gestionnaire = connecter(utilisateur(2L, Role.GESTIONNAIRE, spn));
        ProfilTuteur profil = profil(tuteur, StatutProfil.EN_ATTENTE);
        profil.setRelevePath("releve.pdf");
        when(profils.findById(3L)).thenReturn(Optional.of(profil));
        when(profils.save(any())).thenAnswer(i -> i.getArgument(0));

        ProfilTuteurDto dto = service.decider(3L, new DecisionProfilRequest(StatutProfil.VALIDE, null));

        assertEquals(StatutProfil.VALIDE, dto.statut());
        assertEquals(gestionnaire.getId(), dto.valideParId());
    }

    @Test
    void validerSansReleveDonneUnConflit() {
        connecter(utilisateur(2L, Role.GESTIONNAIRE, spn));
        when(profils.findById(3L)).thenReturn(Optional.of(profil(tuteur, StatutProfil.EN_ATTENTE)));

        assertThrows(ConflictException.class,
                () -> service.decider(3L, new DecisionProfilRequest(StatutProfil.VALIDE, null)));
    }

    @Test
    void refuserSansMotifEstInterdit() {
        connecter(utilisateur(2L, Role.GESTIONNAIRE, spn));
        when(profils.findById(3L)).thenReturn(Optional.of(profil(tuteur, StatutProfil.EN_ATTENTE)));

        assertThrows(IllegalArgumentException.class,
                () -> service.decider(3L, new DecisionProfilRequest(StatutProfil.REFUSE, "   ")));
    }

    @Test
    void unRefusEnregistreLeMotif() {
        connecter(utilisateur(2L, Role.GESTIONNAIRE, spn));
        when(profils.findById(3L)).thenReturn(Optional.of(profil(tuteur, StatutProfil.EN_ATTENTE)));
        when(profils.save(any())).thenAnswer(i -> i.getArgument(0));

        ProfilTuteurDto dto = service.decider(3L,
                new DecisionProfilRequest(StatutProfil.REFUSE, " Relevé incomplet "));

        assertEquals(StatutProfil.REFUSE, dto.statut());
        assertEquals("Relevé incomplet", dto.motifRefus());
    }

    @Test
    void unProfilDejaTraiteNePeutPasEtreRedecide() {
        connecter(utilisateur(2L, Role.GESTIONNAIRE, spn));
        when(profils.findById(3L)).thenReturn(Optional.of(profil(tuteur, StatutProfil.VALIDE)));

        assertThrows(ConflictException.class,
                () -> service.decider(3L, new DecisionProfilRequest(StatutProfil.REFUSE, "motif")));
    }

    @Test
    void unGestionnaireNeDecidePasPourUnAutreEtablissement() {
        connecter(utilisateur(2L, Role.GESTIONNAIRE, etablissement(2L)));
        when(profils.findById(3L)).thenReturn(Optional.of(profil(tuteur, StatutProfil.EN_ATTENTE)));

        assertThrows(AccessDeniedException.class,
                () -> service.decider(3L, new DecisionProfilRequest(StatutProfil.VALIDE, null)));
        verify(profils, never()).save(any());
    }

    @Test
    void laDecisionDoitEtreValideOuRefuse() {
        assertThrows(IllegalArgumentException.class,
                () -> service.decider(3L, new DecisionProfilRequest(StatutProfil.EN_ATTENTE, null)));
        verifyNoInteractions(profils);
    }

    @Test
    void releveAbsentDonneUn404PourLeGestionnaire() {
        connecter(utilisateur(2L, Role.GESTIONNAIRE, spn));
        when(profils.findById(3L)).thenReturn(Optional.of(profil(tuteur, StatutProfil.EN_ATTENTE)));

        assertThrows(NotFoundException.class, () -> service.relevePourGestionnaire(3L));
    }

    // --- Outils ---

    private static Utilisateur connecter(Utilisateur u) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                u, null, List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole()))));
        return u;
    }

    private static Etablissement etablissement(Long id) {
        Etablissement e = new Etablissement();
        e.setId(id);
        e.setNom("Etablissement " + id);
        return e;
    }

    private static Utilisateur utilisateur(Long id, Role role, Etablissement etab) {
        Utilisateur u = new Utilisateur();
        u.setId(id);
        u.setRole(role);
        u.setEtablissement(etab);
        u.setEmail("u" + id + "@tutofac.fr");
        return u;
    }

    private static ProfilTuteur profil(Utilisateur u, StatutProfil statut) {
        ProfilTuteur p = new ProfilTuteur();
        p.setUtilisateur(u);
        p.setFormation("Master Informatique");
        p.setNiveauEtudes(NiveauEtudes.M1);
        p.setStatut(statut);
        return p;
    }
}
