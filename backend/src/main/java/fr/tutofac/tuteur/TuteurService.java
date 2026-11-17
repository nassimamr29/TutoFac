package fr.tutofac.tuteur;

import fr.tutofac.auth.CurrentUser;
import fr.tutofac.config.ConflictException;
import fr.tutofac.config.NotFoundException;
import fr.tutofac.matiere.Matiere;
import fr.tutofac.matiere.MatiereRepository;
import fr.tutofac.utilisateur.Role;
import fr.tutofac.utilisateur.Utilisateur;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class TuteurService {

    private static final long MAX_RELEVE_SIZE = 5 * 1024 * 1024;
    private final ProfilTuteurRepository profils;
    private final MatiereRepository matieres;
    private final Path relevesDirectory;

    public TuteurService(ProfilTuteurRepository profils, MatiereRepository matieres,
                         @Value("${app.upload.releves-dir:uploads/releves}") Path relevesDirectory) {
        this.profils = profils;
        this.matieres = matieres;
        this.relevesDirectory = relevesDirectory.toAbsolutePath().normalize();
    }

    @Transactional
    public ProfilTuteurDto monProfil() {
        return ProfilTuteurDto.from(trouverProfil(CurrentUser.get()));
    }

    @Transactional
    public ProfilTuteurDto enregistrerProfil(ProfilTuteurRequest request) {
        Utilisateur utilisateur = CurrentUser.get();
        exigerTuteur(utilisateur);
        Set<Long> ids = new HashSet<>(request.matiereIds());
        if (ids.size() != request.matiereIds().size()) {
            throw new IllegalArgumentException("La liste des matières contient des doublons");
        }
        List<Matiere> matieresChoisies = matieres.findAllById(ids);
        if (matieresChoisies.size() != ids.size()) {
            throw new IllegalArgumentException("Une ou plusieurs matières n'existent pas");
        }

        ProfilTuteur profil = profils.findByUtilisateurId(utilisateur.getId()).orElseGet(() -> {
            ProfilTuteur nouveau = new ProfilTuteur();
            nouveau.setUtilisateur(utilisateur);
            return nouveau;
        });
        profil.setFormation(request.formation().trim());
        profil.setNiveauEtudes(request.niveauEtudes());
        profil.setMatieres(new HashSet<>(matieresChoisies));
        remettreEnAttente(profil);
        return ProfilTuteurDto.from(profils.save(profil));
    }

    @Transactional
    public ProfilTuteurDto deposerReleve(MultipartFile fichier) {
        Utilisateur utilisateur = CurrentUser.get();
        exigerTuteur(utilisateur);
        ProfilTuteur profil = trouverProfil(utilisateur);
        validerFichier(fichier);

        try {
            Files.createDirectories(relevesDirectory);
            String nomFichier = UUID.randomUUID() + ".pdf";
            Path destination = relevesDirectory.resolve(nomFichier).normalize();
            if (!destination.startsWith(relevesDirectory)) {
                throw new IllegalStateException("Chemin de stockage invalide");
            }
            try (var input = fichier.getInputStream()) {
                Files.copy(input, destination);
            }
            profil.setRelevePath(nomFichier);
            remettreEnAttente(profil);
            return ProfilTuteurDto.from(profils.save(profil));
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible d'enregistrer le relevé de notes", e);
        }
    }

    @Transactional
    public List<ProfilTuteurDto> profilsEnAttente() {
        Long etablissementId = CurrentUser.etablissementId();
        if (etablissementId == null) {
            throw new IllegalArgumentException("Le gestionnaire doit être rattaché à un établissement");
        }
        return profils.findByUtilisateurEtablissementIdAndStatutOrderByCreatedAtAsc(
                        etablissementId, StatutProfil.EN_ATTENTE)
                .stream().map(ProfilTuteurDto::from).toList();
    }

    @Transactional
    public ProfilTuteurDto decider(Long profilId, DecisionProfilRequest request) {
        if (request.decision() != StatutProfil.VALIDE && request.decision() != StatutProfil.REFUSE) {
            throw new IllegalArgumentException("La décision doit être VALIDE ou REFUSE");
        }
        Utilisateur gestionnaire = CurrentUser.get();
        ProfilTuteur profil = trouverProfil(profilId);
        Long etablissementTuteur = profil.getUtilisateur().getEtablissement() == null
                ? null : profil.getUtilisateur().getEtablissement().getId();
        if (!java.util.Objects.equals(gestionnaire.getEtablissement() == null
                ? null : gestionnaire.getEtablissement().getId(), etablissementTuteur)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Un gestionnaire ne peut traiter que les profils de son établissement");
        }
        if (profil.getStatut() != StatutProfil.EN_ATTENTE) {
            throw new ConflictException("Seuls les profils EN_ATTENTE peuvent être traités");
        }
        if (request.decision() == StatutProfil.VALIDE && profil.getRelevePath() == null) {
            throw new ConflictException("Le tuteur doit déposer son relevé de notes avant validation");
        }
        String motif = request.motifRefus() == null ? "" : request.motifRefus().trim();
        if (request.decision() == StatutProfil.REFUSE && motif.isEmpty()) {
            throw new IllegalArgumentException("Un motif est obligatoire pour refuser le profil");
        }
        profil.setStatut(request.decision());
        profil.setMotifRefus(request.decision() == StatutProfil.REFUSE ? motif : null);
        profil.setValidePar(gestionnaire);
        return ProfilTuteurDto.from(profils.save(profil));
    }

    @Transactional
    public Path relevePourGestionnaire(Long profilId) {
        ProfilTuteur profil = trouverProfil(profilId);
        Long etablissementGestionnaire = CurrentUser.etablissementId();
        Long etablissementTuteur = profil.getUtilisateur().getEtablissement() == null
                ? null : profil.getUtilisateur().getEtablissement().getId();
        if (!java.util.Objects.equals(etablissementGestionnaire, etablissementTuteur)) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Le relevé demandé n'appartient pas à votre établissement");
        }
        if (profil.getRelevePath() == null) {
            throw new NotFoundException("Aucun relevé n'a été déposé");
        }
        Path fichier = relevesDirectory.resolve(profil.getRelevePath()).normalize();
        if (!fichier.startsWith(relevesDirectory) || !Files.isRegularFile(fichier)) {
            throw new NotFoundException("Le relevé demandé est introuvable");
        }
        return fichier;
    }

    private ProfilTuteur trouverProfil(Utilisateur utilisateur) {
        return profils.findByUtilisateurId(utilisateur.getId())
                .orElseThrow(() -> new NotFoundException("Profil tuteur introuvable"));
    }

    private ProfilTuteur trouverProfil(Long id) {
        return profils.findById(id).orElseThrow(() -> new NotFoundException("Profil tuteur introuvable"));
    }

    private void exigerTuteur(Utilisateur utilisateur) {
        if (utilisateur.getRole() != Role.TUTEUR) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "Cette action est réservée aux tuteurs");
        }
    }

    private void remettreEnAttente(ProfilTuteur profil) {
        profil.setStatut(StatutProfil.EN_ATTENTE);
        profil.setMotifRefus(null);
        profil.setValidePar(null);
    }

    private void validerFichier(MultipartFile fichier) {
        if (fichier == null || fichier.isEmpty()) {
            throw new IllegalArgumentException("Le relevé de notes est obligatoire");
        }
        if (fichier.getSize() > MAX_RELEVE_SIZE) {
            throw new IllegalArgumentException("Le relevé de notes ne peut pas dépasser 5 Mo");
        }
        if (!"application/pdf".equalsIgnoreCase(fichier.getContentType())) {
            throw new IllegalArgumentException("Le relevé doit être un fichier PDF");
        }
        try {
            byte[] signature = fichier.getBytes();
            if (signature.length < 5 || signature[0] != '%' || signature[1] != 'P'
                    || signature[2] != 'D' || signature[3] != 'F' || signature[4] != '-') {
                throw new IllegalArgumentException("Le fichier transmis n'est pas un PDF valide");
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Impossible de lire le relevé de notes", e);
        }
    }
}